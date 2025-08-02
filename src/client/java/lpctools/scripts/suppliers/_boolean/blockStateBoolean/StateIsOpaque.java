package lpctools.scripts.suppliers._boolean.blockStateBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

public class StateIsOpaque extends BlockStateBoolean {
	public StateIsOpaque(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "stateIsOpaque";
	public static final String fullKey = fullPrefix + nameKey;
	@Override protected boolean getBoolean(BlockState state) {
		return state.isOpaque();
	}
}
