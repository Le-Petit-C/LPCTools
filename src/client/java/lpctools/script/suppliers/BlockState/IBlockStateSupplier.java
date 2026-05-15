package lpctools.script.suppliers.BlockState;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockStateSupplier extends IScriptSupplierNotNull<BlockState> {
	@Override default Class<? extends BlockState> getSuppliedClass(){return BlockState.class;}
}
