package lpctools.script.suppliers.randoms;

import lpctools.script.CompileEnvironment;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

public interface IRandomSupplier<T> extends IScriptSupplier<T> {
	@Override default @NotNull ScriptFunction<RuntimeVariableMap, T>
	compile(CompileEnvironment variableMap){
		var func = compileRandom(variableMap);
		var targetClass = getSuppliedClass();
		return runtimeVariableMap->{
			var res = func.scriptApply(runtimeVariableMap);
			if(res == null || targetClass.isInstance(res))
				//noinspection unchecked
				return (T)res;
			else throw ScriptRuntimeException.notInstanceOf(this, res, targetClass);
		};
	}
	@NotNull ScriptFunction<RuntimeVariableMap, Object>
	compileRandom(CompileEnvironment variableMap);
}
