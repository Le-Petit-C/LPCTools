package lpctools.util;

import lpctools.LPCTools;
import lpctools.util.javaex.QuietAutoCloseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

// 你应当保证存储的值先于此CachedSupplier“无用化”，特别是在存储Closeable对象或定义了自己的furtherInvalidator时
// 一般来说应当是随用随取的，不应该在别的地方长时间保存其中的值
// 功能与Suppplier不同，也因此未令此类实现Suppplier接口
// 清理操作中不应包含对CachedSupplier的get()操作
public class CachedSupplier<T> implements QuietAutoCloseable {
	private final CachedSupplierCore<T> core;
	
	public CachedSupplier(@NotNull Supplier<T> supplier, @Nullable Consumer<T> furtherInvalidator){
		core = new CachedSupplierCore<>(supplier, furtherInvalidator);
		cleaner.register(this, core);
	}
	public CachedSupplier(@NotNull Supplier<T> supplier){ this(supplier, null); }
	
	public @NotNull T get() { return core.get(); }
	public void invalidate() { core.invalidate(); }
	
	public static void clearAllCache(){
		while(true) {
			CachedSupplierCore<?> core;
			synchronized (allValidatedCachedSuppliers) {
				if(allValidatedCachedSuppliers.isEmpty()) break;
				core = allValidatedCachedSuppliers.getLast();
			}
			core.invalidate();
		}
	}
	
	@Override public void close() { invalidate(); }
	
	private static class CachedSupplierCore<T> implements Runnable {
		final Supplier<T> supplier;
		final @Nullable Consumer<T> furtherInvalidator;
		volatile T cachedValue;
		volatile int indexInAll = -1;
		CachedSupplierCore(Supplier<T> supplier, @Nullable Consumer<T> furtherInvalidator) {
			this.supplier = supplier;
			this.furtherInvalidator = furtherInvalidator;
		}
		void invalidate() {
			if(cachedValue != null){
				T oldValue;
				synchronized(this) {
					oldValue = cachedValue;
					if(oldValue != null) {
						cachedValue = null;
						synchronized (allValidatedCachedSuppliers) {
							CachedSupplierCore<?> last = allValidatedCachedSuppliers.getLast();
							last.indexInAll = indexInAll;
							allValidatedCachedSuppliers.set(indexInAll, last);
							allValidatedCachedSuppliers.removeLast();
						}
					}
				}
				if(oldValue != null) {
					try {
						if(furtherInvalidator != null) furtherInvalidator.accept(oldValue);
						else if(oldValue instanceof AutoCloseable closeable) closeable.close();
					} catch (Exception e) {
						LPCTools.LOGGER.warn(e);
					}
				}
			}
		}
		T get() {
			T value = cachedValue;
			if(value == null) {
				synchronized (this) {
					if(cachedValue == null) {
						cachedValue = supplier.get();
						if(cachedValue != null) {
							synchronized (allValidatedCachedSuppliers) {
								indexInAll = allValidatedCachedSuppliers.size();
								allValidatedCachedSuppliers.add(this);
							}
						}
					}
					value = cachedValue;
				}
			}
			return value;
		}
		@Override public void run() { invalidate(); }
	}
	
	private static final Cleaner cleaner = Cleaner.create();
	private static final ArrayList<CachedSupplierCore<?>> allValidatedCachedSuppliers = new ArrayList<>();
}
