package lpctools.script.runtimeInterfaces;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface ScriptDoubleSupplier extends ScriptNotNullSupplier<Double> {
	@Deprecated default @NotNull Double scriptApply(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException{return scriptApplyAsDouble(map);}
	double scriptApplyAsDouble(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException;
}
