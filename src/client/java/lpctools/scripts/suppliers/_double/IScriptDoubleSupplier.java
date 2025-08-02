package lpctools.scripts.suppliers._double;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public interface IScriptDoubleSupplier extends IScriptSupplier<Double> {
	String fullPrefix = IScriptSupplier.fullPrefix + "double.";
	@Override @Deprecated @NotNull default Function<CompiledVariableList, Double>
	compile(VariableMap variableMap) throws CompileFailedException{
		ToDoubleFunction<CompiledVariableList> func = compileToDouble(variableMap);
		return func::applyAsDouble;
	}
	@NotNull ToDoubleFunction<CompiledVariableList> compileToDouble(VariableMap variableMap) throws CompileFailedException;
}
