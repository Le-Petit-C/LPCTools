package lpctools.script.runtimeInterfaces;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface ScriptNotNullSupplier<U> extends ScriptNullableSupplier<U> {
	@NotNull U scriptApply(CompileEnvironment.RuntimeVariableMap  map) throws ScriptRuntimeException;
}
