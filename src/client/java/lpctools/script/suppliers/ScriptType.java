package lpctools.script.suppliers;

import lpctools.script.IScriptWithSubScript;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Function;

public interface ScriptType {
	TypeGenerics<?> generics();
	boolean isAssignableFrom(ScriptType another);
	boolean isInstance(Object obj);
	Text name();
	String id();
	
	static ScriptType getType(Class<?> clazz){
		return TypeData.getType(clazz);
	}
	static ScriptType getType(Object obj){
		return TypeData.getType(obj.getClass());
	}
	
	interface TypeGenerics<T>{
		Class<T> basicClass();
		IScriptSupplier<? extends T> allocateDefault(IScriptWithSubScript parent);
		default @Nullable <U> TypeGenerics<U> checkType(Class<U> clazz){
			if(clazz.equals(basicClass()))
				//noinspection unchecked
				return (TypeGenerics<U>) this;
			else return null;
		}
		record BasicGeneric<T>(Class<T> basicClass, Function<IScriptWithSubScript, IScriptSupplier<? extends T>> defaultAllocator)
			implements TypeGenerics<T>{
			@Override public IScriptSupplier<? extends T> allocateDefault(IScriptWithSubScript parent) {
				return defaultAllocator.apply(parent);
			}
		}
	}
	
	record BasicType<T>(TypeGenerics<T> generics, Text name, String id) implements ScriptType {
		BasicType(Class<T> basicClass, Function<IScriptWithSubScript, IScriptSupplier<? extends T>> defaultAllocator, Text name, String id){
			this(new TypeGenerics.BasicGeneric<>(basicClass, defaultAllocator), name, id);
		}
		@Override public boolean isAssignableFrom(ScriptType another) {
			return generics().basicClass().isAssignableFrom(another.generics().basicClass());
		}
		@Override public boolean isInstance(Object obj) {
			return generics().basicClass().isInstance(obj);
		}
	}
}

class TypeData{
	static final HashMap<Class<?>, ScriptType> typeMap = new HashMap<>();
	static ScriptType getType(Class<?> clazz){
		if(typeMap.containsKey(clazz))
			return typeMap.get(clazz);
		var map = ScriptSupplierLake.typeMap;
		Class<?> superClass = Object.class;
		for(var typeClass : map.keySet()){
			if(typeClass.isAssignableFrom(clazz) && superClass.isAssignableFrom(typeClass))
				superClass = typeClass;
		}
		var res = ScriptSupplierLake.typeMap.get(superClass);
		typeMap.put(clazz, res);
		return res;
	}
}
