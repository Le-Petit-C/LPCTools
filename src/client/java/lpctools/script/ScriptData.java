package lpctools.script;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;
import java.util.function.Consumer;

//存储脚本系统的所有静态数据
public class ScriptData {
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
}
