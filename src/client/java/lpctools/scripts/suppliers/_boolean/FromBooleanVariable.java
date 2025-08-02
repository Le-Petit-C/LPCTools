package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.BooleanVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

public class FromBooleanVariable extends UniqueStringConfig implements IScriptBooleanSupplier {
	public FromBooleanVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), BooleanVariable.testPack);
		return list->list.<MutableBoolean>getVariable(index).booleanValue();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
