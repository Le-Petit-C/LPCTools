package lpctools.util.inGame;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static lpctools.util.MathUtils.square;

abstract class EntityOperationRunner<T extends EntityOperationRunner.EntityOperation<T, ?>,
	U, V extends Enum<V> & InGameOperationRunner.CalculatorGenerator<U>>
	extends InGameOperationRunner<T, U, V> {
	private final Object2ObjectOpenHashMap<Entity, Collection<T>> map = new Object2ObjectOpenHashMap<>();

	Collection<T> getOperations(Entity entity) { return map.get(entity); }

	EntityOperationRunner(EnumArrayOptionListConfig<V> bypassMethodConfig)
	{ super(bypassMethodConfig); }

	interface EntityOperation<T extends EntityOperation<T, W>, W extends Enum<W> & InGameOperation.ResultMarkedState> extends InGameOperation<T, W> {
		Entity getEntity();
	}

	abstract static class BasicEntityOperation
		<T extends BasicEntityOperation<T, W, R>, W extends Enum<W> & InGameOperation.ResultMarkedState, R extends InGameOperationRunner<T, ?, ?>>
		extends BasicOperation<T, W, R> implements EntityOperation<T, W> {
		private final Entity entity;

		BasicEntityOperation(Entity entity, W initState) {
			super(initState);
			this.entity = entity;
		}

		@Override public Entity getEntity() { return entity; }
	}

	protected abstract boolean forEachAction(TickOperationBasicData<U> data, Entity entity, Collection<T> operationInstances);

	@Override protected boolean isEmpty() { return map.isEmpty(); }

	@Override protected void updateCached(T instance) {
		if (instance.isRemoved()) {
			Entity entity = instance.getEntity();
			if (map.get(entity) instanceof Collection<T> collection) {
				collection.remove(instance);
				if (collection.isEmpty()) map.remove(entity);
			}
		} else {
			map.compute(instance.getEntity(), (_, old) -> {
				if (old != null) {
					if (old.size() == 16 && old instanceof ObjectArraySet<?>)
						return new ObjectLinkedOpenHashSet<>(old);
					else return old;
				} else return new ObjectArraySet<>();
			}).add(instance);
		}
	}

	@Override protected void action(InGameOperationRunner.TickOperationBasicData<U> data) {
		double reachSqr = square(data.manager().entityInteractionRange());
		Vec3 playerEye = data.playerEyePos();
		if (data.limit().hasReservedTimes()) {
			// 按玩家距离从近到远排序所有实体
			ArrayList<Map.Entry<Entity, Collection<T>>> entries = new ArrayList<>(map.entrySet());
			entries.sort(Comparator.comparingDouble(e -> e.getKey().distanceToSqr(playerEye)));
			for (var entry : entries) {
				Entity entity = entry.getKey();
				// 实体已失效（消失/移除）：取消其所有操作并清出 map
				if (entity.getRemovalReason() instanceof Entity.RemovalReason removalReason) {
					String reason = removalReason.name().toLowerCase();
					for (T op : entry.getValue())
						op.cancel(Component.translatable("lpctools.utils.inGame.operation.entityGone", reason));
					entry.getValue().clear();
					map.remove(entity);
					continue;
				}
				if (entity.distanceToSqr(playerEye) >= reachSqr) break;
				boolean shouldContinue = true;
				Iterator<T> it = entry.getValue().iterator();
				while (it.hasNext()) {
					var instance = it.next();
					if (!instance.isRemoved()) shouldContinue = false;
					else it.remove();
				}
				if (shouldContinue) continue;
				if (forEachAction(data, entity, entry.getValue())) break;
				if (!data.limit().hasReservedTimes()) break;
			}
		}
	}

	@Override protected void clear() {
		for (Collection<T> collection : map.values())
			for (T instance : collection)
				instance.cancel();
		map.clear();
		super.clear();
	}
}
