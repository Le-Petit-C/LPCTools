package lpctools.script.runtimeInterfaces;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface ScriptIntegerSupplier extends ScriptNotNullSupplier<Integer> {
	@Deprecated default @NotNull Integer scriptApply(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException{return scriptApplyAsInt(map);}
	int scriptApplyAsInt(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException;
}
