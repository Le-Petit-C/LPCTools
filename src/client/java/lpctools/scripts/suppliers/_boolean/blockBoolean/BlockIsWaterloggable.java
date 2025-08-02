package lpctools.scripts.suppliers._boolean.blockBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.Block;
import net.minecraft.block.Waterloggable;
import org.jetbrains.annotations.NotNull;

public class BlockIsWaterloggable extends BlockBoolean{
	public BlockIsWaterloggable(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected boolean getBoolean(Block block) {return block instanceof Waterloggable;}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockIsWaterloggable";
	public static final String fullKey = fullPrefix + nameKey;
	
}
