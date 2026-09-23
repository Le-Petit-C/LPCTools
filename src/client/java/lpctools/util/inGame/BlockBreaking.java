package lpctools.util.inGame;

import lpctools.util.GameTime;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static lpctools.generic.Bypassing.blockBreakBypass;
import static lpctools.util.inGame.BlockBreakBypassMethod.*;
import static lpctools.util.inGame.BlockOperationRunner.*;

public class BlockBreaking extends BasicBlockOperation<BlockBreaking, BlockBreaking.BreakState, BlockBreaking.BlockBreakRunner> {
	public enum BreakState implements ResultMarkedState{
		SUCCEEDED(true, true),
		BREAKING(false, false),
		WAITING(false, false),
		CANCELED(true, false);
		public final boolean isResultState, succeeded;
		BreakState(boolean isResultState, boolean succeeded) {
			this.isResultState = isResultState;
			this.succeeded = succeeded;
		}
		@Override public boolean isResultState() { return isResultState; }
		@Override public boolean succeeded() { return succeeded; }
	}

	@Override public BlockBreaking getThis() { return this; }
	@Override public BreakState getCancelState() { return BreakState.CANCELED; }

	@Override
	@NonNull BlockBreakRunner getRunner() { return runner; }

	public static BlockBreaking scheduleBreak(BlockPos pos) { return new BlockBreaking(pos); }

	public static BlockBreaking scheduleRemoveOnResultBreakIfAbsent(Map<BlockPos, BlockBreaking> map, BlockPos pos) {
		return map.computeIfAbsent(pos.immutable(), p->{
			var res = BlockBreaking.scheduleBreak(p);
			res.appendRemoveOnResultCallback(map);
			return res;
		});
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

	private BlockBreaking(BlockPos pos) {
		super(pos, BreakState.WAITING);
		scheduleUpdate();
	}

	public static boolean progressedLastTick() { return runner.lastProgressTick + 1 >= GameTime.getClientTickCount(); }

	private static final BlockBreakRunner runner = new BlockBreakRunner();

	static class BlockBreakRunner extends BlockOperationRunner<BlockBreaking, StatusCalculator, BlockBreakBypassMethod> {
		long lastProgressTick;

		BlockBreakRunner() { super(blockBreakBypass); }

		@Override protected boolean prepare(TickOperationBasicData<StatusCalculator> data) {
			return !data.manager().gameModeExtraData().continueBreakUpdatedThisTick();
		}

		@Override protected boolean forEachPrepare(TickOperationBasicData<StatusCalculator> data) {
			return !data.manager().isDestroying();
		}

		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, BlockPos pos, Collection<BlockBreaking> operationInstances) {
			// assert !data.manager().isDestroying();
			if(data.bypassCalculator().getHitDirection(pos) instanceof Direction direction) {
				BreakState resultState;
				if(!data.manager().getBlockState(pos).isAir()) {
					data.manager().startDestroyBlock(pos, direction);
					data.limit().costBreakBlock();
					resultState = data.manager().isDestroying() ? BreakState.BREAKING : BreakState.SUCCEEDED;
				}
				else resultState = BreakState.SUCCEEDED;
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
					breakState(pos, BreakState.WAITING);
				}
				else {
					if (data.manager().continueDestroyBlock(pos, direction)) {
						data.manager().addBreakingBlockEffect(pos, direction);
						data.manager().swing(InteractionHand.MAIN_HAND);
					}
				}
				if(!data.manager().isDestroying()) breakState(pos, BreakState.SUCCEEDED);
				else lastProgressTick = GameTime.getClientTickCount();
			}
		}
		private void breakState(BlockPos pos, BreakState state) { breakState(getOperations(pos), state); }
		private void breakState(Collection<BlockBreaking> breakings, BreakState state) {
			if(breakings == null) return;
			for(BlockBreaking breaking : breakings) breaking.setState(state);
		}
	}
}
