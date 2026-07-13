package lpctools.script.runtimeInterfaces;

import lpctools.script.CompileEnvironment;
import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.Nullable;

public interface ScriptNullableSupplier<U> {
	@Nullable U scriptApply(CompileEnvironment.RuntimeVariableMap map) throws ScriptRuntimeException;
}
