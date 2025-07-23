package lpctools.scripts.suppliers.block;

import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.block.Block;

public interface IScriptBlockSupplier extends IScriptSupplier<Block> {
	String fullPrefix = IScriptSupplier.fullPrefix + "block.";
}
