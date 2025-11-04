package lpctools.script.suppliers.Block;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.block.Block;

public interface IBlockSupplier extends IScriptSupplier<Block> {
	@Override default Class<? extends Block> getSuppliedClass(){return Block.class;}
}
