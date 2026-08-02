package lpctools.util.inGame;

import it.unimi.dsi.fastutil.objects.ObjectBooleanBiConsumer;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import lpctools.util.MathUtils;
import lpctools.util.data.SimpleSpaceOctreeMap;
import lpctools.util.data.minecraft.Vector3fEx;
import lpctools.util.mixin.PlayerRotManaging;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import static lpctools.generic.Bypassing.bypassing;
import static lpctools.util.MathUtils.square;

abstract class BlockOperationRunner<T extends BlockOperationRunner.Operation,
	U, V extends Enum<V> & BlockOperationRunner.CalculatorGenerator<U>>
	implements ClientTickEvents.EndTick, ClientLevelEvents.AfterClientLevelChange {
	private final HashSet<T> instancesToUpdate = new HashSet<>();
	private final SimpleSpaceOctreeMap<Collection<T>> map = new SimpleSpaceOctreeMap<>();
	private final EnumArrayOptionListConfig<V> bypassMethodConfig;
	private boolean lastRegistered = false;

	BlockOperationRunner(EnumArrayOptionListConfig<V> bypassMethodConfig) { this.bypassMethodConfig = bypassMethodConfig; }

	void registerAll(boolean b) {
		if (b == lastRegistered) return;
		lastRegistered = b;
		Registries.END_CLIENT_TICK.register(this, b);
		Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
	}

	void addInstanceToUpdate(T instance) {
		instancesToUpdate.add(instance);
		registerAll(true);
	}

	Collection<T> getOperations(BlockPos pos) { return map.get(pos); }

	interface Operation {
		boolean isRemoved();
		BlockPos getPos();
		void cancel();
	}


	interface OperationExA<T extends OperationExA<T>> extends Operation {
		@Nullable Consumer<T> getCallback();
		void setCallback(@Nullable Consumer<T> callback);
		@Contract("->this") T getThis();
		@Contract("_->this") default T appendCallback(@NotNull Consumer<T> callback) {
			Consumer<T> lastCallback = getCallback();
			if(lastCallback == null) setCallback(callback);
			else setCallback((instance)->{
				lastCallback.accept(instance);
				callback.accept(instance);
			});
			return getThis();
		}
		default T putToMap(Map<? super BlockPos, T> map) { return map.put(getPos(), getThis()); }
	}

	interface OperationExB<W extends Enum<W> & OperationExB.ResultMarkedState> extends Operation {
		interface ResultMarkedState { boolean isResultState(); }
		W getState();
		void setState(W state);
		W getCancelState();
		@Override default void cancel() { if(!getState().isResultState()) setState(getCancelState()); }
		@Override default boolean isRemoved() { return getState().isResultState(); }
	}

	interface OperationExC<T extends OperationExC<T, W>, W extends Enum<W> & OperationExB.ResultMarkedState> extends OperationExA<T>, OperationExB<W> {
		default T appendOnResultCallback(@NotNull ObjectBooleanBiConsumer<T> callback) {
			return appendCallback(instance -> { if(instance.isRemoved()) callback.accept(getThis(), getState() != getCancelState()); });
		}
		default T appendRemoveOnResultCallback(Map<BlockPos, BlockBreaking> map) {
			return appendOnResultCallback((breaking, _) -> map.remove(breaking.getPos()));
		}
	}

	interface CalculatorGenerator<T> { T createCalculator(InGameManager manager); }

	@Override public void onEndTick(@NonNull Minecraft client) {
		if (client.isPaused()) return;
		InGameManager manager = InGameManager.get(client);

		// 更新缓存的breakings
		if (manager == null) {
			clear();
			assert map.isEmpty();
		} else {
			for (T instance : instancesToUpdate) {
				if (instance.isRemoved()) {
					BlockPos operationPos = instance.getPos();
					if (map.get(operationPos) instanceof Collection<T> collection) {
						collection.remove(instance);
						if (collection.isEmpty()) map.remove(operationPos);
					}
				} else {
					Collection<T> collection = map.compute(instance.getPos(), (_, _, _, old) -> {
						if (old != null) {
							if (old.size() == 16 && old instanceof ArrayList) return new HashSet<>(old);
							else return old;
						} else return new ArrayList<>();
					});
					collection.add(instance);
				}
			}
			instancesToUpdate.clear();
		}
		if (map.isEmpty()) {
			registerAll(false);
			return;
		}

		float oldYRotRaw = manager.getYRotRaw(), oldXRotRaw = manager.getXRotRaw();
		V bypassMethodRaw = bypassMethodConfig.get();
		@SuppressWarnings("unchecked")
		V bypassMethod = bypassing.getBooleanValue() ? bypassMethodRaw : ((Class<? extends V>)bypassMethodRaw.getClass()).getEnumConstants()[0];
		U bypassCalculator = bypassMethod.createCalculator(manager);

		try (var rotSet = PlayerRotManaging.closerRotSet(manager)) {
			manager.setRotRaw(manager.yRotLastRaw(), manager.xRotLastRaw());
			TickOperationBasicData<U> data = new TickOperationBasicData<>(manager, rotSet, bypassCalculator);
			BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
			if (prepare(data)) {
				if (data.limit.hasReservedTimes() && forEachPrepare(data)) {
					for (var entry : map.fromClosestBounds(data.playerEyePos)) {
						entry.getKey(cache);
						if (MathUtils.cycledClosestDistanceToFullCubeSquared(data.playerEyePos, cache) >= data.reachSqr)
							break;
						// 允许一个 Operation remove 后 cancel 其他 operation(s)，故可能状态会有变动，此处需要检查
						boolean shouldContinue = true;
						for (var instance : entry.getValue()) {
							if (!instance.isRemoved()) {
								shouldContinue = false;
								break;
							}
						}
						if (shouldContinue) continue;
						if (forEachAction(data, cache, entry.getValue())) break;
						if (!data.limit.hasReservedTimes()) break;
					}
				}
				afterAction(data);
			}
		} finally {
			manager.setRotRaw(oldYRotRaw, oldXRotRaw);
		}
	}

	record TickOperationBasicData<U>(InGameManager manager, OperationSpeedLimit limit, PlayerRotManaging.CloserRotSet rotSet, U bypassCalculator, Vec3 playerEyePos, double reachSqr, Vector3fEx vec3fCache) {
		TickOperationBasicData(InGameManager manager, PlayerRotManaging.CloserRotSet rotSet, U bypassCalculator) {
			this(manager, OperationSpeedLimit.root(), rotSet, bypassCalculator, manager.playerEyePos(), square(manager.blockInteractionRange()), new Vector3fEx());
		}
	}

	// tick action 前的准备，返回值决定要不要执行本 tick 操作
	protected abstract boolean prepare(TickOperationBasicData<U> data);
	// forEach 循环前的准备，返回值决定要不要执行 forEach 操作
	protected abstract boolean forEachPrepare(TickOperationBasicData<U> data);
	protected abstract boolean forEachAction(TickOperationBasicData<U> data, BlockPos pos, Collection<T> operationInstances);
	protected abstract void afterAction(TickOperationBasicData<U> data);

	@Override
	public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) {
		clear();
	}

	private void clear() {
		for (Collection<T> collection : map.values())
			for (T instance : collection)
				instance.cancel();
		map.clear();
		for (T instance : instancesToUpdate)
			instance.cancel();
		instancesToUpdate.clear();
	}
}
