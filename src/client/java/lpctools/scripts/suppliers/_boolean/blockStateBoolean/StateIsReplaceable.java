package lpctools.scripts.suppliers._boolean.blockStateBoolean;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

public class StateIsReplaceable extends BlockStateBoolean {
	public StateIsReplaceable(ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "stateIsReplaceable";
	public static final String fullKey = fullPrefix + nameKey;
	@Override protected boolean getBoolean(BlockState state) {
		return state.isReplaceable();
	}
}
