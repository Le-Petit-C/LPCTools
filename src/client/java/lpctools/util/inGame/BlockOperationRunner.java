package lpctools.util.inGame;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import lpctools.util.MathUtils;
import lpctools.util.data.SimpleSpaceOctreeMap;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Contract;

import java.util.*;

abstract class BlockOperationRunner<T extends BlockOperationRunner.BlockOperation<T, ?>,
	U, V extends Enum<V> & InGameOperationRunner.CalculatorGenerator<U>>
	extends InGameOperationRunner<T, U, V> {
	private final SimpleSpaceOctreeMap<Collection<T>> map = new SimpleSpaceOctreeMap<>();

	Collection<T> getOperations(BlockPos pos) { return map.get(pos); }

	BlockOperationRunner(EnumArrayOptionListConfig<V> bypassMethodConfig)
	{ super(bypassMethodConfig); }

	interface BlockOperation<T extends BlockOperation<T, W>, W extends Enum<W> & InGameOperation.ResultMarkedState> extends InGameOperation<T, W> {
		BlockPos getPos();
		@Contract("_->this")
		default T appendRemoveOnResultCallback(Map<BlockPos, T> map) {
			return appendOnResultCallback((instance, _) -> map.remove(instance.getPos()));
		}
	}

	abstract static class BasicBlockOperation
		<T extends BasicBlockOperation<T, W, R>, W extends Enum<W> & InGameOperation.ResultMarkedState, R extends InGameOperationRunner<T, ?, ?>>
		extends BasicOperation<T, W, R> {
		private final BlockPos pos;

		BasicBlockOperation(BlockPos pos, W initState) {
			super(initState);
			this.pos = pos.immutable();
		}

		@Override public BlockPos getPos() { return pos; }
	}

	// tick action 前的准备，返回值决定要不要执行本 tick 操作
	protected abstract boolean prepare(TickOperationBasicData<U> data);
	// forEach 循环前的准备，返回值决定要不要执行 forEach 操作
	protected abstract boolean forEachPrepare(TickOperationBasicData<U> data);
	protected abstract boolean forEachAction(TickOperationBasicData<U> data, BlockPos pos, Collection<T> operationInstances);
	protected abstract void afterAction(TickOperationBasicData<U> data);


	@Override protected boolean isEmpty() { return map.isEmpty(); }

	@Override protected void updateCached(T instance) {
		if (instance.isRemoved()) {
			BlockPos operationPos = instance.getPos();
			if (map.get(operationPos) instanceof Collection<T> collection) {
				collection.remove(instance);
				if (collection.isEmpty()) map.remove(operationPos);
			}
		} else {
			map.compute(instance.getPos(), (_, _, _, old) -> {
				if (old != null) {
					if (old.size() == 16 && old instanceof ObjectArraySet<?>)
						return new ObjectLinkedOpenHashSet<>(old);
					else return old;
				} else return new ObjectArraySet<>();
			}).add(instance);
		}
	}

	@Override protected void action(InGameOperationRunner.TickOperationBasicData<U> data) {
		BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
		if (prepare(data)) {
			if (data.limit().hasReservedTimes() && forEachPrepare(data)) {
				for (var entry : map.fromClosestBounds(data.playerEyePos())) {
					entry.getKey(cache);
					if (MathUtils.cycledClosestDistanceToFullCubeSquared(data.playerEyePos(), cache) >= data.reachSqr())
						break;
					// 允许一个 Operation remove 后 cancel 其他 operation(s)，故可能状态会有变动，此处需要检查
					boolean shouldContinue = true;
					Iterator<T> it = entry.getValue().iterator();
					while (it.hasNext()) {
						var instance = it.next();
						if (!instance.isRemoved()) shouldContinue = false;
						else it.remove();
					}
					if (shouldContinue) continue;
					if (forEachAction(data, cache, entry.getValue())) break;
					if (!data.limit().hasReservedTimes()) break;
				}
			}
			afterAction(data);
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
