package lpctools.scripts.suppliers.direction;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.DirectionVariable;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FromDirectionVariable extends UniqueStringConfig implements IScriptDirectionSupplier {
	public FromDirectionVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull Function<CompiledVariableList, Direction>
	compile(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), DirectionVariable.testPack);
		return list->list.getVariable(index);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
