package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueDoubleConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class StaticDouble extends UniqueDoubleConfig implements IScriptDoubleSupplier {
	public StaticDouble(ILPCConfigReadable parent) {
		super(parent, nameKey, 0);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "staticDouble";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) {
		double staticValue = getDoubleValue();
		return list-> staticValue;
	}
}
