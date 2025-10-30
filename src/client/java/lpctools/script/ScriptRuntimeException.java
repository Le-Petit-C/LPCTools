package lpctools.script;

import fi.dy.masa.malilib.util.StringUtils;
import lpctools.script.suppliers.IScriptSupplier;

public class ScriptRuntimeException extends Exception {
	public ScriptRuntimeException(String message) {super(message);}
	
	public static ScriptRuntimeException ofTranslate(IScriptSupplier<?> script, String key, Object ...args){
		var res = new ScriptRuntimeException(StringUtils.translate(key, args));
		ScriptData.putRuntimeException(script, res);
		return res;
	}
	public static ScriptRuntimeException notInstanceOf(IScriptSupplier<?> scriptSupplier, Object o, Class<?> clazz) {
		return ofTranslate(scriptSupplier, "lpctools.script.exception.runtime.notInstanceOf", clazz.getName(), o.getClass().getName());
	}
}
