package lpctools.script.suppliers.Vec3d;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.world.phys.Vec3;

public interface IVec3dSupplier extends IScriptSupplierNotNull<Vec3> {
	@Override default Class<? extends Vec3> getSuppliedClass(){return Vec3.class;}
}
