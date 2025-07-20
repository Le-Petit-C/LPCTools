package lpctools.scripts.suppliers.interfaces;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;

import java.util.function.Function;

public interface IScriptSupplier<T> extends IScriptBase {
	String fullPrefix = IScriptBase.fullPrefix + "suppliers.";
	Function<CompiledVariableList, T> compile(VariableMap variableMap) throws CompileFailedException;
}
