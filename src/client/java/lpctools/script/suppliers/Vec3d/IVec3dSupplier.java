package lpctools.script.suppliers.Vec3d;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.util.math.Vec3d;

public interface IVec3dSupplier extends IScriptSupplier<Vec3d> {
	@Override default Class<? extends Vec3d> getSuppliedClass(){return Vec3d.class;}
}
