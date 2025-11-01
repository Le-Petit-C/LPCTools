package lpctools.script.runtimeInterfaces;

import lpctools.script.exceptions.ScriptRuntimeException;

public interface ScriptFunction<T, U> {
	U scriptApply(T val) throws ScriptRuntimeException;
}
