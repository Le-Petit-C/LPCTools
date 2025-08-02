package lpctools.scripts.suppliers.axis;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.AxisVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FromAxisVariable extends UniqueStringConfig implements IScriptAxisSupplier {
	public FromAxisVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull Function<CompiledVariableList, Direction.Axis>
	compile(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), AxisVariable.testPack);
		return list->list.getVariable(index);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
