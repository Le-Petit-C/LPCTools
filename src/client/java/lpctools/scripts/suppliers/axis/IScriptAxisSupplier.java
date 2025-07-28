package lpctools.scripts.suppliers.axis;

import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.util.math.Direction;

public interface IScriptAxisSupplier extends IScriptSupplier<Direction.Axis> {
	String fullPrefix = IScriptSupplier.fullPrefix + "axis.";
}
