package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class StaticInt extends UniqueIntegerConfig implements IScriptIntSupplier {
	public StaticInt(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "staticInt";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) {
		int value = getIntegerValue();
		return list->value;
	}
}
