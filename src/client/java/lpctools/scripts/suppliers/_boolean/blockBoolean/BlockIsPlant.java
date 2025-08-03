package lpctools.scripts.suppliers._boolean.blockBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.*;
import org.jetbrains.annotations.NotNull;

public class BlockIsPlant extends BlockBoolean{
	public BlockIsPlant(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected boolean getBoolean(Block block) {
		return block instanceof PlantBlock || block instanceof AbstractPlantPartBlock;
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockIsPlant";
	public static final String fullKey = fullPrefix + nameKey;
}
