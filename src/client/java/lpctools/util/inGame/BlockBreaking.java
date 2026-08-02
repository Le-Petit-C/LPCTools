package lpctools.util.inGame;

import lpctools.util.GameTime;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static lpctools.generic.Bypassing.blockBreakingBypass;
import static lpctools.util.inGame.BlockBreakBypassMethod.*;
import static lpctools.util.inGame.BlockOperationRunner.*;

public class BlockBreaking implements OperationExC<BlockBreaking, BlockBreaking.BreakingState> {
	public enum BreakingState implements ResultMarkedState{
		SUCCEEDED(true),
		BREAKING(false),
		WAITING(false),
		CANCELED(true);
		public final boolean isResultState;
		BreakingState(boolean isResultState) { this.isResultState = isResultState; }
		@Override public boolean isResultState() { return isResultState; }
	}

	private @Nullable Consumer<BlockBreaking> callback;
	private final BlockPos pos;
	private BreakingState state = BreakingState.WAITING;

	@Override public BlockPos getPos() { return pos; }
	@Override public @Nullable Consumer<BlockBreaking> getCallback() { return callback; }
	@Override public void setCallback(@Nullable Consumer<BlockBreaking> callback) { this.callback = callback; }
	@Override public BlockBreaking getThis() { return this; }
	@Override public BreakingState getState() { return state; }
	@Override public void setState(BreakingState state) {
		if(this.state == state) return;
		if(this.state.isResultState != state.isResultState) scheduleUpdate();
		this.state = state;
		if(callback != null) callback.accept(this);
	}
	@Override public BreakingState getCancelState() { return BreakingState.CANCELED; }

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

	private BlockBreaking(BlockPos pos) { this.pos = pos.immutable(); }

	private void scheduleUpdate() { runner.addInstanceToUpdate(this); }

	public static boolean progressedLastTick() {
		return runner.lastProgressTick + 1 >= GameTime.getClientTickCount();
	}

	static final BlockBreakingRunner runner = new BlockBreakingRunner();

	static class BlockBreakingRunner extends BlockOperationRunner<BlockBreaking, StatusCalculator, BlockBreakBypassMethod> {
		long lastProgressTick;

		BlockBreakingRunner() { super(blockBreakingBypass); }

		@Override protected boolean prepare(TickOperationBasicData<StatusCalculator> data) {
			return !data.manager().gameModeExtraData().continueBreakUpdatedThisTick();
		}

		@Override protected boolean forEachPrepare(TickOperationBasicData<StatusCalculator> data) {
			return !data.manager().isDestroying();
		}

		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, BlockPos pos, Collection<BlockBreaking> operationInstances) {
			// assert !data.manager().isDestroying();
			if(data.bypassCalculator().getHitDirection(pos) instanceof Direction direction) {
				BreakingState resultState;
				if(!data.manager().getBlockState(pos).isAir()) {
					data.manager().startDestroyBlock(pos, direction);
					data.limit().costBreakBlock();
					resultState = data.manager().isDestroying() ? BreakingState.BREAKING : BreakingState.SUCCEEDED;
				}
				else resultState = BreakingState.SUCCEEDED;
				breakState(operationInstances, resultState);
			}
			if(!data.manager().isDestroying()) {
				if(!data.manager().getBlockState(pos).isAir()) {
					if(data.bypassCalculator().getTargetDirection(pos, data.vec3fCache()))
						data.rotSet().setIfCloser(data.vec3fCache());
				}
			}
			return data.manager().isDestroying();
		}

		@Override protected void afterAction(TickOperationBasicData<StatusCalculator> data) {
			if (data.manager().isDestroying()) {
				BlockPos pos = data.manager().getDestroyBlockPos();
				if (data.bypassCalculator().getTargetDirection(pos, data.vec3fCache()))
					data.rotSet().setIfCloser(data.vec3fCache());
				Direction direction = data.bypassCalculator().getHitDirection(pos);
				if(direction == null) {
					data.manager().stopDestroyBlock();
					breakState(pos, BreakingState.WAITING);
				}
				else {
					if (data.manager().continueDestroyBlock(pos, direction)) {
						data.manager().addBreakingBlockEffect(pos, direction);
						data.manager().swing(InteractionHand.MAIN_HAND);
					}
				}
				if(!data.manager().isDestroying()) breakState(pos, BreakingState.SUCCEEDED);
				else lastProgressTick = GameTime.getClientTickCount();
			}
		}
		private void breakState(BlockPos pos, BreakingState state) { breakState(getOperations(pos), state); }
		private void breakState(Collection<BlockBreaking> breakings, BreakingState state) {
			if(breakings == null) return;
			for(BlockBreaking breaking : breakings) breaking.setState(state);
		}
	};
}
