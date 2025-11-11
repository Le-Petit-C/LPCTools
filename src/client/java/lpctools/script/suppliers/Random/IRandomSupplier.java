package lpctools.script.suppliers.Random;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

public interface IRandomSupplier<T> extends IScriptSupplier<T> {
	@Override default @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, T>
	compile(CompileEnvironment variableMap){
		var func = compileRandom(variableMap);
		var targetClass = getSuppliedClass();
		if(targetClass != Object.class) return runtimeVariableMap->{
			var res = func.scriptApply(runtimeVariableMap);
			if(res == null || targetClass.isInstance(res))
				//noinspection unchecked
				return (T)res;
			else throw ScriptRuntimeException.notInstanceOf(this, res, targetClass);
		};
		else //noinspection unchecked
			return (ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, T>) func;
	}
	@NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Object>
	compileRandom(CompileEnvironment variableMap);
}
