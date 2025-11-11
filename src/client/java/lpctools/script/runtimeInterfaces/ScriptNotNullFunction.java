package lpctools.script.runtimeInterfaces;

import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface ScriptNotNullFunction<T, U> extends ScriptNullableFunction<T, U>{
	@NotNull U scriptApply(T val) throws ScriptRuntimeException;
}
