package lpctools.script.suppliers;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.suppliers.Random.IRandomSupplierAllocator;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class ScriptRegistration<T, U extends IScriptSupplier<? extends T>> implements ChooseScreen.OptionCallback<Consumer<ScriptRegistration<T, U>>> {
	public final String key;
	public final Component displayName;
	public final Component comment;
	public final Class<U> supplierClass;
	@Override public void action(ButtonBase button, int mouseButton, Consumer<ScriptRegistration<T, U>> userData) {userData.accept(this);}
	public abstract @Nullable <V> IScriptSupplierAllocator<? extends IScriptSupplier<V>>
	tryAllocate(Class<V> targetType);
	private ScriptRegistration(String key, Component displayName, Component comment, Class<U> supplierClass) {
		this.key = key;
		this.supplierClass = supplierClass;
		this.displayName = displayName;
		this.comment = comment;
	}
	
	public static <T, U extends IScriptSupplier<T>> ScriptRegistration<T, U> ofPrecise(String key, Component displayName, Component comment, Class<T> clazz, Class<U> supplierClass, IScriptSupplierAllocator<U> allocator){
		return new ScriptRegistration<>(key, displayName, comment, supplierClass) {
			@Override public @Nullable <V> IScriptSupplierAllocator<? extends IScriptSupplier<V>>
			tryAllocate(Class<V> targetType) {
				if(targetType.isAssignableFrom(clazz))
					//noinspection unchecked
					return (IScriptSupplierAllocator<? extends IScriptSupplier<V>>) allocator;
				else return null;
			}
		};
	}
	
	public static <U extends IScriptSupplier<?>> ScriptRegistration<Object, U> ofRandom(String key, Component displayName, Component comment, Class<U> supplierClass, IRandomSupplierAllocator allocator){
		return new ScriptRegistration<>(key, displayName, comment, supplierClass) {
			@Override public <V> @NotNull IScriptSupplierAllocator<IScriptSupplier<V>>
			tryAllocate(Class<V> targetType) {return parent->allocator.allocate(parent, targetType);}
		};
	}
}
