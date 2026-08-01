package lpctools.util.inGame;

import it.unimi.dsi.fastutil.objects.ObjectBooleanBiConsumer;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.GameTime;
import lpctools.util.MathUtils;
import lpctools.util.data.SimpleSpaceOctreeMap;
import lpctools.util.data.minecraft.Vector3fEx;
import lpctools.util.javaex.QuietAutoCloseable;
import lpctools.util.mixin.PlayerRotManaging;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.BiConsumer;

import static lpctools.util.MathUtils.square;

public class BlockBreaking {
	public enum BreakingState {
		SUCCEEDED(true),
		BREAKING(false),
		WAITING(false),
		CANCELED(true);
		public final boolean isResultState;
		BreakingState(boolean isResultState) { this.isResultState = isResultState; }
	}

	private BiConsumer<BlockBreaking, BreakingState> callback;
	private final BlockPos pos;
	private BreakingState state = BreakingState.WAITING;
	private boolean updated = true;

	public BreakingState getState() { return state; }
	public BlockPos getPos() { return pos; }
	@SuppressWarnings("UnusedReturnValue")
	public BlockBreaking putToMap(Map<? super BlockPos, BlockBreaking> map) { return map.put(pos, this); }
	public boolean isRemoved() { return getState().isResultState; }

	@Contract("_->this")
	public BlockBreaking appendCallback(@NotNull BiConsumer<BlockBreaking, BreakingState> callback) {
		if(this.callback == null) this.callback = callback;
		else {
			BiConsumer<BlockBreaking, BreakingState> lastCallback = this.callback;
			this.callback = (instance, state)->{
				lastCallback.accept(instance, state);
				callback.accept(instance, state);
			};
		}
		return this;
	}

	public BlockBreaking appendOnResultCallback(@NotNull ObjectBooleanBiConsumer<BlockBreaking> callback) {
		return appendCallback((breaking, state) -> { if(state.isResultState) callback.accept(breaking, state == BreakingState.SUCCEEDED); });
	}

	public BlockBreaking appendRemoveOnResultCallback(Map<BlockPos, BlockBreaking> map) {
		return appendOnResultCallback((breaking, _) -> map.remove(breaking.getPos()));
	}

	public static BlockBreaking scheduleBreak(BlockPos pos) {
		BlockBreaking instance = new BlockBreaking(pos);
		instance.scheduleUpdate();
		return instance;
	}

	public static BlockBreaking scheduleRemoveOnResultBreakIfAbsent(Map<BlockPos, BlockBreaking> map, BlockPos pos) {
		return map.computeIfAbsent(pos.immutable(), p->BlockBreaking.scheduleBreak(p).appendRemoveOnResultCallback(map));
	}

	public static BlockBreakingCollection createBreakingCollection() { return new BlockBreakingCollection(); }

	public static class BlockBreakingCollection implements QuietAutoCloseable {
		private final HashMap<BlockPos, BlockBreaking> breakingCache = new HashMap<>();
		private final HashSet<BlockPos> posCache = new HashSet<>();
		public interface BreakingScheduler extends QuietAutoCloseable { void scheduleBreak(BlockPos pos); }
		public BreakingScheduler startUpdateBreakings() {
			posCache.addAll(breakingCache.keySet());
			return new BreakingScheduler() {
				@Override public void scheduleBreak(BlockPos pos) {
					scheduleRemoveOnResultBreakIfAbsent(breakingCache, pos);
					posCache.remove(pos);
				}
				@Override public void close() {
					posCache.forEach(p->breakingCache.remove(p).cancel());
					posCache.clear();
				}
			};
		}
		@Override public void close() {
			ArrayList<BlockBreaking> stored = new ArrayList<>(breakingCache.values());
			stored.forEach(BlockBreaking::cancel);
			breakingCache.clear();
		}
	}

	public void cancel() {
		if(state != BreakingState.CANCELED && state != BreakingState.SUCCEEDED) {
			setState(BreakingState.CANCELED);
			scheduleUpdate();
		}
	}

	private BlockBreaking(BlockPos pos) { this.pos = pos.immutable(); }

	private void setState(BreakingState state) {
		if(this.state == state) return;
		this.state = state;
		if(callback != null) callback.accept(this, state);
	}

	private void scheduleUpdate() {
		if(!updated) return;
		Runner.instance.instancesToUpdate.add(this);
		Runner.instance.registerAll(true);
		updated = false;
	}

	public static boolean progressedLastTick() {
		return Runner.instance.lastProgressTick + 1 >= GameTime.getClientTickCount();
	}

	private static class Runner implements ClientTickEvents.EndTick, ClientLevelEvents.AfterClientLevelChange {
		static final Runner instance = new Runner();
		final ArrayList<BlockBreaking> instancesToUpdate = new ArrayList<>();
		final SimpleSpaceOctreeMap<Collection<BlockBreaking>> map = new SimpleSpaceOctreeMap<>();
		boolean lastRegistered = false;
		long lastProgressTick;
		void registerAll(boolean b) {
			if(b == lastRegistered) return;
			lastRegistered = b;
			Registries.END_CLIENT_TICK.register(this, b);
			Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
		}
		@Override public void onEndTick(@NonNull Minecraft client) {
			if(client.isPaused()) return;
			InGameManager manager = InGameManager.get(client);
			var limit = OperationSpeedLimit.root();

			// 更新缓存的breakings
			if(manager == null) {
				clear();
				assert map.isEmpty();
			}
			else {
				for(BlockBreaking instance : instancesToUpdate) {
					if(instance.state == BreakingState.CANCELED) {
						if(map.get(instance.pos) instanceof Collection<BlockBreaking> collection) {
							collection.remove(instance);
							if(collection.isEmpty()) map.remove(instance.pos);
						}
					}
					else {
						instance.setState(BreakingState.WAITING);
						Collection<BlockBreaking> collection = map.compute(instance.pos, (_, _, _, old)->{
							if(old != null) {
								if(old.size() == 16 && old instanceof ArrayList) return new HashSet<>(old);
								else return old;
							}
							else return new ArrayList<>();
						});
						collection.add(instance);
					}
					instance.updated = true;
				}
				instancesToUpdate.clear();
			}
			if(map.isEmpty()) {
				registerAll(false);
				return;
			}

			if(manager.gameModeExtraData().continueBreakUpdatedThisTick()) return;

			// 处理需要处理的breaking

			float oldYRotRaw = manager.getYRotRaw(), oldXRotRaw = manager.getXRotRaw();

			try (var rotSet = PlayerRotManaging.closerRotSet(manager)) {
				manager.setRotRaw(manager.yRotLastRaw(), manager.xRotLastRaw());
				Vec3 playerEyePos = manager.playerEyePos();
				double reachSqr = square(manager.blockInteractionRange());
				BlockBreakingBypassMethod.StatusCalculator bypassCalculator = BlockBreakingBypassMethod.current().createCalculator(manager);
				BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
				Vec3i expand = new Vec3i(1, 1, 1);
				Vector3fEx targetDirection = new Vector3fEx();

				if(!manager.isDestroying() && limit.hasReservedTimes()) {
					for(var entry : map.fromClosestBounds(playerEyePos)) {
						entry.getKey(cache);
						if(MathUtils.cycledClosestDistanceSquared(playerEyePos, cache, expand) >= reachSqr) break;
						if(bypassCalculator.getHitDirection(cache) != null) {
							if(!manager.getBlockState(cache).isAir()) {
								manager.startDestroyBlock(cache, manager.playerNearstViewDirection().getOpposite());
								limit.costBreakBlock();
								if(manager.isDestroying()) {
									breakState(cache, BreakingState.BREAKING);
									break;
								}
							}
							breakSuccess(cache);
							if(!limit.hasReservedTimes()) break;
						}
					}
				}

				if(manager.isDestroying() && bypassCalculator.getTargetDirection(manager.getDestroyBlockPos(), targetDirection))
					rotSet.setIfCloser(targetDirection);
				else {
					for(var entry : map.fromClosestBounds(playerEyePos)) {
						entry.getKey(cache);
						if(MathUtils.cycledClosestDistanceSquared(playerEyePos, cache, expand) >= reachSqr) break;
						if(manager.getBlockState(cache).isAir()) continue;
						if(bypassCalculator.getTargetDirection(cache, targetDirection))
							rotSet.setIfCloser(targetDirection);
					}
				}

				if(manager.isDestroying()) {
					BlockPos pos = manager.getDestroyBlockPos();
					Direction direction = bypassCalculator.getHitDirection(pos);
					if(direction == null) {
						manager.stopDestroyBlock();
						breakState(pos, BreakingState.WAITING);
					}
					else {
						if (manager.continueDestroyBlock(pos, direction)) {
							manager.addBreakingBlockEffect(pos, direction);
							manager.swing(InteractionHand.MAIN_HAND);
						}
					}
					if(!manager.isDestroying()) breakSuccess(pos);
					else lastProgressTick = GameTime.getClientTickCount();
				}
			}
			finally {
				manager.setRotRaw(oldYRotRaw, oldXRotRaw);
			}
		}

		@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) { clear(); }
		private void breakState(BlockPos pos, BreakingState state) {
			Collection<BlockBreaking> breakings = map.get(pos);
			for(BlockBreaking breaking : breakings) breaking.setState(state);
		}
		private void breakSuccess(BlockPos pos) {
			Collection<BlockBreaking> breakings = map.remove(pos);
			for(BlockBreaking breaking : breakings) breaking.setState(BreakingState.SUCCEEDED);
		}
		private void clear() {
			for(BlockBreaking instance : instancesToUpdate)
				instance.setState(BreakingState.CANCELED);
			instancesToUpdate.clear();
			for(Collection<BlockBreaking> collection : map.values())
				for(BlockBreaking instance : collection)
					instance.setState(BreakingState.CANCELED);
			map.clear();
		}
	}
}
