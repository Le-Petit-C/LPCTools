package lpctools.script.suppliers;

import lpctools.script.CompileTimeVariableMap;
import lpctools.script.IScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IScriptSupplier<T> extends IScript {
	@Nullable String getName();
	
	Class<? extends T> getSuppliedClass();
	@NotNull ScriptFunction<RuntimeVariableMap, T>
	compile(CompileTimeVariableMap variableMap);
}
