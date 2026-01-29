package lpctools.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CachedSupplier<T> {
	private final Supplier<@NotNull T> supplier;
	private final @Nullable Consumer<@NotNull T> furtherInvalidator;
	private @Nullable T cachedValue;
	public CachedSupplier(@NotNull Supplier<@NotNull T> supplier, @Nullable Consumer<@NotNull T> furtherInvalidator){
		this.supplier = supplier;
		this.furtherInvalidator = furtherInvalidator;
	}
	public CachedSupplier(@NotNull Supplier<@NotNull T> supplier){
		this(supplier, null);
	}
	public @NotNull T get(){
		if(cachedValue == null) cachedValue = supplier.get();
		return cachedValue;
	}
	public void invalidate(){
		if(cachedValue != null){
			if(furtherInvalidator != null)
				furtherInvalidator.accept(cachedValue);
			cachedValue = null;
		}
	}
}
