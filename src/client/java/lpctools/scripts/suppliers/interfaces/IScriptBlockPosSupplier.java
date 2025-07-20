package lpctools.scripts.suppliers.interfaces;

import net.minecraft.util.math.BlockPos;

public interface IScriptBlockPosSupplier extends IScriptSupplier<BlockPos> {
	String fullPrefix = IScriptSupplier.fullPrefix + "blockPos.";
}
