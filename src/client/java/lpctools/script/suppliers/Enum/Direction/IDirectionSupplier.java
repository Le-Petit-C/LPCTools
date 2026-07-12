package lpctools.script.suppliers.Enum.Direction;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.core.Direction;

public interface IDirectionSupplier extends IScriptSupplierNotNull<Direction> {
	@Override default Class<? extends Direction> getSuppliedClass(){return Direction.class;}
}
