package lpctools.script.suppliers;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.suppliers.randoms.IRandomSupplierAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class ScriptRegistration<T, U extends IScriptSupplier<T>> implements ChooseScreen.OptionCallback<Consumer<ScriptRegistration<T, U>>> {
	public final String key;
	public final Class<U> supplierClass;
	@Override public void action(ButtonBase button, int mouseButton, Consumer<ScriptRegistration<T, U>> userData) {userData.accept(this);}
	public abstract @Nullable <V> IScriptSupplierAllocator<? extends IScriptSupplier<V>>
	tryAllocate(Class<V> targetType);
	private ScriptRegistration(String key, Class<U> supplierClass){this.key = key; this.supplierClass = supplierClass;}
	
	public static <T, U extends IScriptSupplier<T>> ScriptRegistration<T, U> ofPrecise(String key, Class<T> clazz, Class<U> supplierClass, IScriptSupplierAllocator<U> allocator){
		return new ScriptRegistration<>(key, supplierClass) {
			@Override public @Nullable <V> IScriptSupplierAllocator<? extends IScriptSupplier<V>>
			tryAllocate(Class<V> targetType) {
				if(targetType.isAssignableFrom(clazz))
					//noinspection unchecked
					return (IScriptSupplierAllocator<? extends IScriptSupplier<V>>) allocator;
				else return null;
			}
		};
	}
	
	public static <U extends IScriptSupplier<Object>> ScriptRegistration<Object, U> ofRandom(String key, Class<U> supplierClass, IRandomSupplierAllocator allocator){
		return new ScriptRegistration<>(key, supplierClass) {
			@Override public <T> @NotNull IScriptSupplierAllocator<IScriptSupplier<T>>
			tryAllocate(Class<T> targetType) {return allocator::allocate;}
		};
	}
}
