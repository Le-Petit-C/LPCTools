package lpctools.scripts.suppliers.blockState;

import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.block.BlockState;

public interface IScriptBlockStateSupplier extends IScriptSupplier<BlockState> {
	String fullPrefix = IScriptSupplier.fullPrefix + "blockState.";
}
