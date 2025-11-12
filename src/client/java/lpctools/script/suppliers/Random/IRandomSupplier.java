package lpctools.script.suppliers.Random;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import lpctools.script.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

public interface IRandomSupplier<T> extends IScriptSupplier<T> {
	@Override default @NotNull ScriptNullableSupplier<T>
	compile(CompileEnvironment environment){
		var func = compileRandom(environment);
		var targetClass = getSuppliedClass();
		if(targetClass != Object.class) return runtimeVariableMap->{
			var res = func.scriptApply(runtimeVariableMap);
			if(res == null || targetClass.isInstance(res))
				//noinspection unchecked
				return (T)res;
			else throw ScriptRuntimeException.notInstanceOf(this, res, targetClass);
		};
		else //noinspection unchecked
			return (ScriptNullableSupplier<T>) func;
	}
	@NotNull ScriptNullableSupplier<Object>
	compileRandom(CompileEnvironment environment);
}
