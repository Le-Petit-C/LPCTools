package lpctools.script.suppliers.Block;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.block.Block;

public interface IBlockSupplier extends IScriptSupplierNotNull<Block> {
	@Override default Class<? extends Block> getSuppliedClass(){return Block.class;}
}
