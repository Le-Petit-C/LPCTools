package lpctools.script.suppliers.Vec3d;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.util.math.Vec3d;

public interface IVec3dSupplier extends IScriptSupplierNotNull<Vec3d> {
	@Override default Class<? extends Vec3d> getSuppliedClass(){return Vec3d.class;}
}
