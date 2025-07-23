package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockPosConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class StaticBlockPos extends BlockPosConfig implements IScriptBlockPosSupplier {
	public StaticBlockPos(ILPCConfigReadable parent) {
		super(parent, nameKey, BlockPos.ORIGIN, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "staticBlockPos";
	public static final String fullKey = fullPrefix + nameKey;
	
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) {
		BlockPos staticPos = getPos().toImmutable();
		return (list, pos)->pos.set(staticPos);
	}
}
