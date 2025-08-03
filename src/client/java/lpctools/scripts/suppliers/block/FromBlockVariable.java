package lpctools.scripts.suppliers.block;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.BlockStateVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FromBlockVariable extends UniqueStringConfig implements IScriptBlockSupplier {
	public FromBlockVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey, null, null);}
	@Override public @NotNull Function<CompiledVariableList, Block>
	compile(VariableMap variableMap) throws CompileFailedException {
		int index = variableMap.get(getStringValue(), BlockStateVariable.testPack);
		return list->list.getVariable(index);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVariable";
	public static final String fullKey = fullPrefix + nameKey;
	
}
