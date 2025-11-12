package lpctools.script.suppliers.Direction;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.util.math.Direction;

public interface IDirectionSupplier extends IScriptSupplierNotNull<Direction> {
	@Override default Class<? extends Direction> getSuppliedClass(){return Direction.class;}
}
