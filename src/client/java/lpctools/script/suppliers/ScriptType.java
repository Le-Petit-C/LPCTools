package lpctools.script.suppliers;

import net.minecraft.text.Text;

import java.util.HashMap;

public interface ScriptType {
	Class<?> basicClass();
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
	
	record BasicType(Class<?> basicClass, Text name, String id) implements ScriptType {
		@Override public boolean isAssignableFrom(ScriptType another) {
			return basicClass.isAssignableFrom(another.basicClass());
		}
		@Override public boolean isInstance(Object obj) {
			return basicClass.isInstance(obj);
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
