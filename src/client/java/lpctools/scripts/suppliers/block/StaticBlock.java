package lpctools.scripts.suppliers.block;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StaticBlock extends BlockConfig implements IScriptBlockSupplier {
	public StaticBlock(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, Blocks.AIR, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull Function<CompiledVariableList, Block>
	compile(VariableMap variableMap){
		Block block = getBlock();
		return list->block;
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "static";
	public static final String fullKey = fullPrefix + nameKey;
}
