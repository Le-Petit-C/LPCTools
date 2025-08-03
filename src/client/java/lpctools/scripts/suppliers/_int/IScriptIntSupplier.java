package lpctools.scripts.suppliers._int;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public interface IScriptIntSupplier extends IScriptSupplier<Integer> {
	String fullPrefix = IScriptSupplier.fullPrefix + "int.";
	@Override @Deprecated @NotNull default Function<CompiledVariableList, Integer>
	compile(VariableMap variableMap) throws CompileFailedException{
		ToIntFunction<CompiledVariableList> func = compileToInt(variableMap);
		return func::applyAsInt;
	}
	@NotNull ToIntFunction<CompiledVariableList> compileToInt(VariableMap variableMap) throws CompileFailedException;
}
