package lpctools.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CachedSupplier<T> implements AutoCloseable {
	public static ArrayList<CachedSupplier<?>> allCachedSuppliers = new ArrayList<>();
	private final Supplier<@NotNull T> supplier;
	private final @Nullable Consumer<@NotNull T> furtherInvalidator;
	private int id;
	private @Nullable T cachedValue;
	public CachedSupplier(@NotNull Supplier<@NotNull T> supplier, @Nullable Consumer<@NotNull T> furtherInvalidator){
		this.supplier = supplier;
		this.furtherInvalidator = furtherInvalidator;
		this.id = allCachedSuppliers.size();
		allCachedSuppliers.add(this);
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
			else if(cachedValue instanceof AutoCloseable closeable){
				try {
					closeable.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			cachedValue = null;
		}
	}
	@Override public void close() {
		invalidate();
		var lastLast = allCachedSuppliers.removeLast();
		if(this != lastLast){
			allCachedSuppliers.set(id, lastLast);
			lastLast.id = id;
		}
		id = -1;
	}
	public static void clearAllCache(){
		for(var cachedSupplier : allCachedSuppliers)
			cachedSupplier.invalidate();
		System.gc();
	}
}
