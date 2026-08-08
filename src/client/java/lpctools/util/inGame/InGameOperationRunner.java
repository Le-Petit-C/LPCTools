package lpctools.util.inGame;

import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import lpctools.util.data.minecraft.Vector3fEx;
import lpctools.util.mixin.PlayerRotManaging;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;

import static lpctools.generic.Bypassing.bypassing;
import static lpctools.util.MathUtils.square;

abstract class InGameOperationRunner<T extends InGameOperation<T, ?>,
	U, V extends Enum<V> & InGameOperationRunner.CalculatorGenerator<U>>
	implements ClientTickEvents.EndTick, ClientLevelEvents.AfterClientLevelChange {
	private final LinkedHashSet<T> instancesToUpdate = new LinkedHashSet<>();
	private final EnumArrayOptionListConfig<V> bypassMethodConfig;
	private boolean lastRegistered = false;

	InGameOperationRunner(EnumArrayOptionListConfig<V> bypassMethodConfig) { this.bypassMethodConfig = bypassMethodConfig; }

	void registerAllRaw(boolean b) {
		Registries.END_CLIENT_TICK.register(this, b);
		Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
	}

	void registerAll(boolean b) {
		if(lastRegistered == b) return;
		registerAllRaw(b);
		lastRegistered = b;
	}

	void addInstanceToUpdate(T instance) {
		instancesToUpdate.add(instance);
		registerAll(true);
	}

	public abstract static class BasicOperation<T extends BasicOperation<T, W, R>, W extends Enum<W> & InGameOperation.ResultMarkedState, R extends InGameOperationRunner<T, ?, ?>>
		implements InGameOperation<T, W>, BlockOperationRunner.BlockOperation<T, W> {
		private @Nullable Consumer<T> callback;
		private W state;
		private @Nullable Component failComponent;

		BasicOperation(W initState) { this.state = initState; }

		abstract @NotNull R getRunner();

		@Override public @Nullable Consumer<T> getCallback() { return callback; }
		@Override public void setCallback(@Nullable Consumer<T> callback) { this.callback = callback; }
		@Override public W getState() { return state; }
		@Override public void setState(W state) {
			if(this.state == state) return;
			this.state = state;
			scheduleUpdate();
			if(getCallback() instanceof Consumer<T> cb) cb.accept(getThis());
		}

		/** 此操作进入失败/取消结果态时的原因（可能为 null）。 */
		@Override public @Nullable Component getFailComponent() { return failComponent; }
		/** 设置失败原因组件（通常在操作进入失败/取消结果态前调用）。 */
		@Override public void setFailComponent(@NotNull Component failComponent) { this.failComponent = failComponent; }

		void scheduleUpdate() { getRunner().addInstanceToUpdate(getThis()); }
	}

	interface CalculatorGenerator<T> { T createCalculator(InGameManager manager); }

	@Override public void onEndTick(@NonNull Minecraft client) {
		if (client.isPaused()) return;
		InGameManager manager = InGameManager.get(client);

		// 更新缓存的instances
		if (manager == null) clear();
		else {
			while (!instancesToUpdate.isEmpty())
				updateCached(instancesToUpdate.removeFirst());
		}
		if (manager == null || isEmpty()) {
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
			action(new TickOperationBasicData<>(manager, rotSet, bypassCalculator));
		} finally {
			manager.setRotRaw(oldYRotRaw, oldXRotRaw);
		}
	}

	record TickOperationBasicData<U>(InGameManager manager, OperationSpeedLimit limit, PlayerRotManaging.CloserRotSet rotSet, U bypassCalculator, Vec3 playerEyePos, double reachSqr, Vector3fEx vec3fCache) {
		TickOperationBasicData(InGameManager manager, PlayerRotManaging.CloserRotSet rotSet, U bypassCalculator) {
			this(manager, OperationSpeedLimit.root(), rotSet, bypassCalculator, manager.playerEyePos(), square(manager.blockInteractionRange()), new Vector3fEx());
		}
	}

	protected abstract boolean isEmpty();
	protected abstract void updateCached(T instance);
	protected abstract void action(TickOperationBasicData<U> data);
	protected void clear() {
		while (!instancesToUpdate.isEmpty())
			instancesToUpdate.removeFirst().cancel();
	}

	@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) { clear(); }
}
