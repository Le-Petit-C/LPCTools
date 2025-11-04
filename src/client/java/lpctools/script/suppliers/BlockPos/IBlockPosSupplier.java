package lpctools.script.suppliers.BlockPos;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.util.math.BlockPos;

public interface IBlockPosSupplier extends IScriptSupplier<BlockPos> {
	@Override default Class<? extends BlockPos> getSuppliedClass(){return BlockPos.class;}
}
