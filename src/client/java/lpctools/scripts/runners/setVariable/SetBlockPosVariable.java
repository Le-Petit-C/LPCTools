package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.VariableTestPack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class SetBlockPosVariable extends SetVariable<BlockPos>{
	public SetBlockPosVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new BlockPosSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return BlockPosVariable.testPack;}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setBlockPosVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
