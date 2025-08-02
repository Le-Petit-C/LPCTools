package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface IScriptBooleanSupplier extends IScriptSupplier<Boolean> {
	String fullKey = IScriptSupplier.fullPrefix + "boolean";
	String fullPrefix = fullKey + '.';
	@Override @Deprecated @NotNull default Function<CompiledVariableList, Boolean>
	compile(VariableMap variableMap) throws CompileFailedException{
		ToBooleanFunction<CompiledVariableList> func = compileToBoolean(variableMap);
		return func::applyAsBoolean;
	}
	@NotNull ToBooleanFunction<CompiledVariableList> compileToBoolean(VariableMap variableMap) throws CompileFailedException;
}
