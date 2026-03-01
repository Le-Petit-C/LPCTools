package lpctools.script;

import com.google.gson.JsonElement;

import java.util.Objects;
import java.util.function.Consumer;

//存储脚本系统的所有静态数据
public class ScriptData {
	private static JsonElement clipBoardJson = null;
	private static Class<?> clipBoardClass = null;
	
	public static void setClipboard(JsonElement json, Class<?> clazz){
		clipBoardJson = json;
		clipBoardClass = clazz;
	}
	//TODO:输出失败原因
	public static boolean pasteTo(Consumer<JsonElement> jsonConsumer, Class<?> checkClass){
		if(clipBoardJson == null) return false;
		if(!Objects.equals(checkClass, clipBoardClass)) return false;
		jsonConsumer.accept(clipBoardJson);
		return true;
	}
}
