package lpctools.util.inGame;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface InGameOperation {
	boolean isRemoved();
	void cancel();

	interface WithCallback<T extends WithCallback<T>> extends InGameOperation {
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
	}

	interface WithState<W extends Enum<W> & WithState.ResultMarkedState> extends InGameOperation {
		interface ResultMarkedState { boolean isResultState(); boolean succeeded(); }
		W getState();
		void setState(W state);
		W getCancelState();
		@Override default void cancel() { if(!getState().isResultState()) setState(getCancelState()); }
		@Override default boolean isRemoved() { return getState().isResultState(); }
	}
}
