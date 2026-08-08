package lpctools.util.inGame;

import it.unimi.dsi.fastutil.objects.ObjectBooleanBiConsumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface InGameOperation<T extends InGameOperation<T, W>,
	W extends Enum<W> & InGameOperation.ResultMarkedState> {
	interface ResultMarkedState { boolean isResultState(); boolean succeeded(); }

	// 回调能力
	@Nullable Consumer<T> getCallback();
	void setCallback(@Nullable Consumer<T> callback);
	@Contract("->this") T getThis();
	@Contract("_->this") default T appendCallback(@NotNull Consumer<T> callback) {
		Consumer<T> lastCallback = getCallback();
		if(lastCallback == null) setCallback(callback);
		else setCallback(instance->{
			lastCallback.accept(instance);
			callback.accept(instance);
		});
		return getThis();
	}
	/** 结果态（成功/失败/取消）时触发回调，携带 (实例, 是否成功)。 */
	@Contract("_->this")
	default T appendOnResultCallback(@NotNull ObjectBooleanBiConsumer<T> callback) {
		return appendCallback(instance -> { if(instance.isRemoved()) callback.accept(getThis(), getState().succeeded()); });
	}

	// 状态能力
	W getState();
	void setState(W state);
	W getCancelState();
	default void cancel() { if(!getState().isResultState()) setState(getCancelState()); }
	default boolean isRemoved() { return getState().isResultState(); }

	// 失败原因能力
	@Nullable Component getFailComponent();
	void setFailComponent(@NotNull Component failComponent);
	/** 带原因的取消；reason 为 null 时使用默认取消消息。 */
	default void cancel(@Nullable Component reason) {
		if(isRemoved()) return;
		if(reason != null) setFailComponent(reason);
			else if(getFailComponent() == null) setFailComponent(Component.translatable("lpctools.utils.inGame.operation.cancelled"));
		cancel();
	}
}
