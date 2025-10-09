package lpctools.script;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;
import java.util.function.Consumer;

//存储脚本系统的所有静态数据
public interface ScriptData {
	MutableObject<JsonElement> clipBoardJson = new MutableObject<>();
	MutableObject<Class<?>> clipBoardClass = new MutableObject<>();
	static void setClipboard(JsonElement json, Class<?> clazz){
		clipBoardJson.setValue(json);
		clipBoardClass.setValue(clazz);
	}
	//TODO:输出失败原因
	static boolean pasteTo(Consumer<JsonElement> jsonConsumer, Class<?> checkClass){
		if(clipBoardJson.getValue() == null) return false;
		if(!Objects.equals(checkClass, clipBoardClass.getValue())) return false;
		jsonConsumer.accept(clipBoardJson.getValue());
		return true;
	}
}
