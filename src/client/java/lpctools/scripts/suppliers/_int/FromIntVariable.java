package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.IntVariable;
import lpctools.scripts.runners.variables.VariableMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class FromIntVariable extends UniqueStringConfig implements IScriptIntSupplier {
	public FromIntVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), IntVariable.testPack);
		return list->list.<MutableInt>getVariable(index).intValue();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
	
}
