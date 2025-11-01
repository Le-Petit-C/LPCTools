package lpctools.script.exceptions;

import fi.dy.masa.malilib.util.StringUtils;
import lpctools.script.IScript;

public class ScriptRuntimeException extends ScriptException {
	public ScriptRuntimeException(String message) {super(message);}
	
	public static ScriptRuntimeException ofTranslate(IScript source, String key, Object ...args){
		return source.putException(new ScriptRuntimeException(StringUtils.translate(key, args)));
	}
	public static ScriptRuntimeException notInstanceOf(IScript source, Object o, Class<?> clazz) {
		return ofTranslate(source, "lpctools.script.exception.runtime.notInstanceOf", clazz.getName(), o.getClass().getName());
	}
	public static ScriptRuntimeException nullPointer(IScript source) {
		return ofTranslate(source, "lpctools.script.exception.runtime.nullPointer");
	}
}
