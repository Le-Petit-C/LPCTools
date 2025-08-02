package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class BlockVariable extends Variable<Block>{
	public BlockVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected Block allocate() {return Blocks.AIR;}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "block";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof BlockVariable, fullKey);
}
