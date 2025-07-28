package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.DoubleVariable;
import lpctools.scripts.runners.variables.VariableMap;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class FromDoubleVariable extends UniqueStringConfig implements IScriptDoubleSupplier {
	public FromDoubleVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), DoubleVariable.testPack);
		return list->list.<MutableDouble>getVariable(index).doubleValue();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
