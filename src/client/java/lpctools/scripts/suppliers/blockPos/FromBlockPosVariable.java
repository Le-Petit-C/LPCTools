package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FromBlockPosVariable extends UniqueStringConfig implements IScriptBlockPosSupplier {
	public FromBlockPosVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull Function<CompiledVariableList, BlockPos> compile(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), v->v instanceof BlockPosVariable, BlockPosVariable.fullKey);
		return list->list.getVariable(index);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromBlockPosVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
