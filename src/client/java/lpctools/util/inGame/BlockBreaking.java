package lpctools.util.inGame;

import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.MultiPlayerGameModeAccessor;
import lpctools.mixinData.MixinData;
import lpctools.util.GameTime;
import lpctools.util.MathUtils;
import lpctools.util.data.SimpleSpaceOctreeMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
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

public class BlockBreaking {
	public enum BreakingState {
		SUCCEEDED,
		BREAKING,
		WAITING,
		CANCELED
	}

	private BiConsumer<BlockBreaking, BreakingState> callback;
	private final BlockPos pos;
	private BreakingState state = BreakingState.WAITING;
	private boolean updated = true;

	public BreakingState getState() { return state; }
	public BlockPos getPos() { return pos; }
	@SuppressWarnings("UnusedReturnValue")
	public BlockBreaking putToMap(Map<? super BlockPos, BlockBreaking> map) { return map.put(pos, this); }
	public boolean isRemoved() { return getState() == BreakingState.SUCCEEDED || getState() == BreakingState.CANCELED; }

	@Contract("_->this")
	public BlockBreaking callback(@NotNull BiConsumer<BlockBreaking, BreakingState> callback) {
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

	public static BlockBreaking scheduleBreak(BlockPos pos) {
		BlockBreaking instance = new BlockBreaking(pos);
		instance.scheduleUpdate();
		return instance;
	}

	public void cancel() {
		setState(BreakingState.CANCELED);
		scheduleUpdate();
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
			LocalPlayer player = client.player;
			MultiPlayerGameMode gameMode = client.gameMode;
			ClientLevel level = client.level;
			if(player == null || gameMode == null || level == null) {
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
			if(MixinData.getData(gameMode).continueBreakUpdatedThisTick()) return;
			Vec3 playerEyePos = player.getEyePosition();
			double reachSqr = MathUtils.square(player.blockInteractionRange());
			if(gameMode.isDestroying()) {
				MultiPlayerGameModeAccessor gameModeAccessor = (MultiPlayerGameModeAccessor)gameMode;
				BlockPos currentPos = gameModeAccessor.getDestroyBlockPos();
				if(MathUtils.minSquaredDistanceToBlock(playerEyePos, currentPos) >= reachSqr) {
					gameMode.stopDestroyBlock();
					breakState(currentPos, BreakingState.WAITING);
				}
			}
			if(!gameMode.isDestroying()) {
				BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
				Vec3i expand = new Vec3i(1, 1, 1);
				for(var entry : map.fromClosestBounds(playerEyePos)) {
					if(MathUtils.cycledSquaredClosestDistance(playerEyePos, entry.getKey(cache), expand) >= reachSqr) break;
					if(!level.getBlockState(cache).isAir()) {
						gameMode.startDestroyBlock(cache, player.getDirection().getOpposite());
						if(gameMode.isDestroying()) {
							breakState(cache, BreakingState.BREAKING);
							break;
						}
					}
					breakSuccess(cache);
				}
			}
			if(gameMode.isDestroying()) {
				MultiPlayerGameModeAccessor gameModeAccessor = (MultiPlayerGameModeAccessor)gameMode;
				BlockPos pos = gameModeAccessor.getDestroyBlockPos();
				Direction direction = player.getDirection().getOpposite();
				if (gameMode.continueDestroyBlock(pos, direction)) {
					level.addBreakingBlockEffect(pos, direction);
					player.swing(InteractionHand.MAIN_HAND);
				}
				if(!gameMode.isDestroying()) breakSuccess(pos);
				else lastProgressTick = GameTime.getClientTickCount();
			}
		}
		@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) {
			clear();
		}
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
