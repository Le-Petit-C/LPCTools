package lpctools.script;

import com.google.gson.JsonElement;
import lpctools.script.suppliers.IScriptSupplier;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

//存储脚本系统的所有静态数据
public class ScriptData {
	public static final HashMap<IScriptSupplier<?>, ArrayList<ScriptRuntimeException>> runtimeExceptions = new HashMap<>();
	private static final MutableObject<JsonElement> clipBoardJson = new MutableObject<>();
	private static final MutableObject<Class<?>> clipBoardClass = new MutableObject<>();
	public static void setClipboard(JsonElement json, Class<?> clazz){
		clipBoardJson.setValue(json);
		clipBoardClass.setValue(clazz);
	}
	//TODO:输出失败原因
	public static boolean pasteTo(Consumer<JsonElement> jsonConsumer, Class<?> checkClass){
		if(clipBoardJson.getValue() == null) return false;
		if(!Objects.equals(checkClass, clipBoardClass.getValue())) return false;
		jsonConsumer.accept(clipBoardJson.getValue());
		return true;
	}
	public static void clearRuntimeExceptions(){runtimeExceptions.clear();}
	public static void putRuntimeException(IScriptSupplier<?> script, ScriptRuntimeException runtimeException){
		runtimeExceptions.computeIfAbsent(script, v->new ArrayList<>()).add(runtimeException);
	}
}
