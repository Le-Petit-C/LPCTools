package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BlockPosVariable extends Variable<BlockPos>{
	public BlockPosVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected BlockPos allocate() {return BlockPos.ORIGIN;}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPos";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack = new VariableTestPack(
		v->v instanceof BlockPosVariable,
		fullKey
	);
}
