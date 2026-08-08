package lpctools.util.inGame;

import lpctools.generic.Bypassing;
import lpctools.generic.OperationSpeedLimit;
import lpctools.util.inGame.BlockInteractBypassMethod.StatusCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class BlockInteraction extends BlockOperationRunner.BasicBlockOperation<BlockInteraction, BlockInteraction.InteractState, BlockInteraction.BlockInteractRunner> {
	public final @Nullable Direction targetInteractDirection;
	// preparation 可用于restock之类的操作
	private final @NotNull Prepare preparation;

	@Override public BlockInteraction getThis() { return this; }
	@Override public InteractState getCancelState() { return InteractState.CANCELLED; }

	@Override
	@NonNull BlockInteractRunner getRunner() { return runner; }

	public enum InteractState implements ResultMarkedState {
		SUCCEEDED(true, true),
		WAITING(false, false),
		CANCELLED(true, false),
		FAILED(true, false);
		public final boolean isResultState, succeeded;
		InteractState(boolean isResultState, boolean succeeded) {
			this.isResultState = isResultState;
			this.succeeded = succeeded;
		}
		@Override public boolean isResultState() { return isResultState; }
		@Override public boolean succeeded() { return succeeded; }
	}

	public interface Prepare { @Nullable InteractionHand prepare(BlockInteraction instance, OperationSpeedLimit limit); }

	public static BlockInteraction scheduleInteract(BlockPos pos, @Nullable Direction targetDirection, @NotNull Prepare preparation) {
		return new BlockInteraction(pos, targetDirection, preparation);
	}

	public static BlockInteraction scheduleInteract(BlockPos pos, @Nullable Direction targetDirection, InteractionHand hand) {
		return scheduleInteract(pos, targetDirection, (_, _)->hand);
	}

	private BlockInteraction(BlockPos pos, @Nullable Direction targetInteractDirection, @NotNull Prepare preparation) {
		super(pos, InteractState.WAITING);
		this.targetInteractDirection = targetInteractDirection;
		this.preparation = preparation;
		scheduleUpdate();
	}

	private static final BlockInteractRunner runner = new BlockInteractRunner();

	static class BlockInteractRunner extends BlockOperationRunner<BlockInteraction, StatusCalculator, BlockInteractBypassMethod> {
		BlockInteractRunner() { super(Bypassing.blockInteractBypass); }
		@Override protected boolean prepare(TickOperationBasicData<StatusCalculator> data) { return true; }
		@Override protected boolean forEachPrepare(TickOperationBasicData<StatusCalculator> data) { return true; }
		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, BlockPos pos, Collection<BlockInteraction> operationInstances) {
			for (BlockInteraction instance : operationInstances) {
				if (instance.isRemoved()) continue;
				if (data.bypassCalculator().getValidInteractHitResult(pos, instance.targetInteractDirection) instanceof BlockHitResult hitResult
					&& instance.preparation.prepare(instance, data.limit()) instanceof InteractionHand hand) {
					if(data.manager().useItemOn(hand, hitResult).consumesAction())
						instance.setState(InteractState.SUCCEEDED);
					else {
						instance.setFailComponent(Component.translatable("lpctools.utils.inGame.operation.blockInteraction.fail", pos));
						instance.setState(InteractState.FAILED);
					}
					data.limit().costInteractBlock();
					if(!data.limit().hasReservedTimes()) break;
				}
				else if (data.bypassCalculator().getBlockInteractDirection(pos, instance.targetInteractDirection, data.vec3fCache()))
					data.rotSet().setIfCloser(data.vec3fCache());
			}
			return false;
		}
		@Override protected void afterAction(TickOperationBasicData<StatusCalculator> data) {}
	}
}
