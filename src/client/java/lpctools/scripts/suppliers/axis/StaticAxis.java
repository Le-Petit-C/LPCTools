package lpctools.scripts.suppliers.axis;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StaticAxis extends ArrayOptionListConfig<Direction.Axis> implements IScriptAxisSupplier {
	public StaticAxis(ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(Direction.Axis axis : Direction.Axis.values())
			addOption(axis.asString(), axis);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "static";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull Function<CompiledVariableList, Direction.Axis>
	compile(VariableMap variableMap){
		Direction.Axis axis = get();
		return list->axis;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {}
}
