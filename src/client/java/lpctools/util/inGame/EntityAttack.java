package lpctools.util.inGame;

import lpctools.generic.Bypassing;
import lpctools.util.inGame.EntityBypassMethod.StatusCalculator;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class EntityAttack extends EntityOperationRunner.BasicEntityOperation<EntityAttack, EntityAttack.AttackState, EntityAttack.EntityAttackRunner> {
	@Override public EntityAttack getThis() { return this; }
	@Override public AttackState getCancelState() { return AttackState.CANCELLED; }
	@Override @NonNull EntityAttackRunner getRunner() { return runner; }

	public enum AttackState implements ResultMarkedState {
		SUCCEEDED(true, true),
		WAITING(false, false),
		CANCELLED(true, false);
		public final boolean isResultState, succeeded;
		AttackState(boolean isResultState, boolean succeeded) {
			this.isResultState = isResultState;
			this.succeeded = succeeded;
		}
		@Override public boolean isResultState() { return isResultState; }
		@Override public boolean succeeded() { return succeeded; }
	}

	public static EntityAttack scheduleAttack(Entity entity) { return new EntityAttack(entity); }

	private EntityAttack(Entity entity) {
		super(entity, AttackState.WAITING);
		scheduleUpdate();
	}

	private static final EntityAttackRunner runner = new EntityAttackRunner();

	static class EntityAttackRunner extends EntityOperationRunner<EntityAttack, StatusCalculator, EntityBypassMethod> {
		EntityAttackRunner() { super(Bypassing.entityAttackBypass); }
		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, Entity entity, Collection<EntityAttack> operationInstances) {
			for (EntityAttack instance : operationInstances) {
				if (instance.isRemoved()) continue;
				if (data.bypassCalculator().isReachable(entity)) {
					data.manager().attack(entity);
					instance.setState(AttackState.SUCCEEDED);
					data.limit().costAttackEntity();
					if (!data.limit().hasReservedTimes()) break;
				}
				else if (data.bypassCalculator().getAimDirection(entity, data.vec3fCache()))
					data.rotSet().setIfCloser(data.vec3fCache());
			}
			return false;
		}
	}
}
