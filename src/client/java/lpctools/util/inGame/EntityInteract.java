package lpctools.util.inGame;

import lpctools.generic.Bypassing;
import lpctools.generic.OperationSpeedLimit;
import lpctools.util.inGame.EntityBypassMethod.StatusCalculator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class EntityInteract extends EntityOperationRunner.BasicEntityOperation<EntityInteract, EntityInteract.InteractState, EntityInteract.EntityInteractRunner> {
	// preparation 可用于restock之类的操作
	private final @NotNull Prepare preparation;

	@Override public EntityInteract getThis() { return this; }
	@Override public InteractState getCancelState() { return InteractState.CANCELLED; }
	@Override @NonNull EntityInteractRunner getRunner() { return runner; }

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

	public interface Prepare { @Nullable InteractionHand prepare(EntityInteract instance, OperationSpeedLimit limit); }

	public static EntityInteract scheduleInteract(Entity entity, @NotNull Prepare preparation) {
		return new EntityInteract(entity, preparation);
	}

	public static EntityInteract scheduleInteract(Entity entity, InteractionHand hand) {
		return scheduleInteract(entity, (_, _)->hand);
	}

	private EntityInteract(Entity entity, @NotNull Prepare preparation) {
		super(entity, InteractState.WAITING);
		this.preparation = preparation;
		scheduleUpdate();
	}

	private static final EntityInteractRunner runner = new EntityInteractRunner();

	static class EntityInteractRunner extends EntityOperationRunner<EntityInteract, StatusCalculator, EntityBypassMethod> {
		EntityInteractRunner() { super(Bypassing.entityInteractBypass); }
		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, Entity entity, Collection<EntityInteract> operationInstances) {
			for (EntityInteract instance : operationInstances) {
				if (instance.isRemoved()) continue;
				if (data.bypassCalculator().isReachable(entity)
					&& instance.preparation.prepare(instance, data.limit()) instanceof InteractionHand hand) {
					if (data.manager().interact(entity, hand).consumesAction())
						instance.setState(InteractState.SUCCEEDED);
					else {
						instance.setFailComponent(Component.translatable("lpctools.utils.inGame.operation.entityInteraction.fail"));
						instance.setState(InteractState.FAILED);
					}
					data.limit().costInteractEntity();
					if (!data.limit().hasReservedTimes()) break;
				}
				else if (data.bypassCalculator().getAimDirection(entity, data.vec3fCache()))
					data.rotSet().setIfCloser(data.vec3fCache());
			}
			return false;
		}
	}
}
