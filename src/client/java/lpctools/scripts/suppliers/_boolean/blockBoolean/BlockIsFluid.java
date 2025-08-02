package lpctools.scripts.suppliers._boolean.blockBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import org.jetbrains.annotations.NotNull;

public class BlockIsFluid extends BlockBoolean{
	public BlockIsFluid(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected boolean getBoolean(Block block) {return block instanceof FluidBlock;}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockIsFluid";
	public static final String fullKey = fullPrefix + nameKey;
}
