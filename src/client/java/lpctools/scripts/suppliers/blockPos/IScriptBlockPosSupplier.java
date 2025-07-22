package lpctools.scripts.suppliers.blockPos;

import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.util.math.BlockPos;

public interface IScriptBlockPosSupplier extends IScriptSupplier<BlockPos> {
	String fullPrefix = IScriptSupplier.fullPrefix + "blockPos.";
}
