package lpctools.script.runtimeInterfaces;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface ScriptBooleanSupplier extends ScriptNotNullSupplier<Boolean> {
	@Deprecated default @NotNull Boolean scriptApply(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException{return scriptApplyAsBoolean(map);}
	boolean scriptApplyAsBoolean(CompileEnvironment.RuntimeVariableMap  map) throws ScriptRuntimeException;
}
