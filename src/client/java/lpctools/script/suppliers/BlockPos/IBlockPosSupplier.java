package lpctools.script.suppliers.BlockPos;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.util.math.BlockPos;

public interface IBlockPosSupplier extends IScriptSupplierNotNull<BlockPos> {
	@Override default Class<? extends BlockPos> getSuppliedClass(){return BlockPos.class;}
}
