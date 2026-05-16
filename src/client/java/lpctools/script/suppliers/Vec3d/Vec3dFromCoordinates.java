package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Vec3dFromCoordinates extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<Double> x = ofStorage(Double.class,
		Component.translatable("lpctools.script.suppliers.vec3d.vec3dFromCoordinates.subSuppliers.x.name"), "x");
	protected final SupplierStorage<Double> y = ofStorage(Double.class,
		Component.translatable("lpctools.script.suppliers.vec3d.vec3dFromCoordinates.subSuppliers.y.name"), "y");
	protected final SupplierStorage<Double> z = ofStorage(Double.class,
		Component.translatable("lpctools.script.suppliers.vec3d.vec3dFromCoordinates.subSuppliers.z.name"), "z");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(x, y, z);
	
	public Vec3dFromCoordinates(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3>
	compileNotNull(CompileEnvironment environment) {
		var compiledDouble1Supplier = compileCheckedDouble(x.get(), environment);
		var compiledDouble2Supplier = compileCheckedDouble(y.get(), environment);
		var compiledDouble3Supplier = compileCheckedDouble(z.get(), environment);
		return map->new Vec3(
			compiledDouble1Supplier.scriptApplyAsDouble(map),
			compiledDouble2Supplier.scriptApplyAsDouble(map),
			compiledDouble3Supplier.scriptApplyAsDouble(map)
		);
	}
}
