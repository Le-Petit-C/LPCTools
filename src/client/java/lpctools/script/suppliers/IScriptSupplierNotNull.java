package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNotNullFunction;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplierNotNull<T> extends IScriptSupplier<T> {
	default @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, T>
	compile(CompileEnvironment variableMap){return compileNotNull(variableMap);}
	@NotNull ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, T>
	compileNotNull(CompileEnvironment variableMap);
	
	static <T> @NotNull ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, T>
	ofNotNull(IScriptSupplier<?> me, @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, T> func){
		if(func instanceof ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap,T> notNullFunction)
			return notNullFunction;
		else return map->{
			var res = func.scriptApply(map);
			if(res == null) throw ScriptRuntimeException.nullPointer(me);
			return res;
		};
	}
}
