package lpctools.script.runtimeInterfaces;

import lpctools.script.exceptions.ScriptRuntimeException;
import org.jetbrains.annotations.Nullable;

public interface ScriptNullableFunction<T, U> {
	@Nullable U scriptApply(T val) throws ScriptRuntimeException;
}
