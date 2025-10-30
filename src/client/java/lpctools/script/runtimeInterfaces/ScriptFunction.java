package lpctools.script.runtimeInterfaces;

import lpctools.script.ScriptRuntimeException;

public interface ScriptFunction<T, U> {
	U scriptApply(T val) throws ScriptRuntimeException;
}
