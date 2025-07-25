package lpctools.scripts.suppliers.direction6;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StaticDirection6 extends ArrayOptionListConfig<Direction> implements IScriptDirection6Supplier {
	public StaticDirection6(ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(Direction direction : Direction.values())
			addOption(direction.asString(), direction);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "staticDirection6";
	public static final String fullKey = fullPrefix + nameKey;
	
	@Override public @NotNull Function<CompiledVariableList, Direction>
	compile(VariableMap variableMap){
		Direction direction = get();
		return list->direction;
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(1, ((button, mouseButton) -> {}), ()->get().asString(), buttonGenericAllocator);
	}
}
