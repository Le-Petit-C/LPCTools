package lpctools.scripts.suppliers._boolean.blockStateBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import org.jetbrains.annotations.NotNull;

public class StateIsWaterloggable extends BlockStateBoolean {
	public StateIsWaterloggable(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "stateIsWaterloggable";
	public static final String fullKey = fullPrefix + nameKey;
	@Override protected boolean getBoolean(BlockState state) {
		return state.getProperties().contains(Properties.WATERLOGGED);
	}
}
