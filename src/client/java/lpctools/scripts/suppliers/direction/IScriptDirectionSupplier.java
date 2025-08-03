package lpctools.scripts.suppliers.direction;

import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.util.math.Direction;

public interface IScriptDirectionSupplier extends IScriptSupplier<Direction> {
	String fullPrefix = IScriptSupplier.fullPrefix + "direction.";
}
