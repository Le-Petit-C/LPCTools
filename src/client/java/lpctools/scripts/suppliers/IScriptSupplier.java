package lpctools.scripts.suppliers;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface IScriptSupplier<T> extends IScriptBase {
	String fullPrefix = IScriptBase.fullPrefix + "suppliers.";
	@NotNull Function<CompiledVariableList, T> compile(VariableMap variableMap) throws CompileFailedException;
}
