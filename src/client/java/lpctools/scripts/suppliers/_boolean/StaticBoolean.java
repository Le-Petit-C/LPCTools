package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

public class StaticBoolean extends UniqueBooleanConfig implements IScriptBooleanSupplier {
	public StaticBoolean(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, false, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "staticBoolean";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) {
		boolean b = getBooleanValue();
		return list->b;
	}
}
