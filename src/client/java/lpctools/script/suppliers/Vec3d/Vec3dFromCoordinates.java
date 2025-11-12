package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Double.ConstantDouble;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class Vec3dFromCoordinates extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<Double> x = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.vec3dFromCoordinates.subSuppliers.x.name"), "x");
	protected final SupplierStorage<Double> y = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.vec3dFromCoordinates.subSuppliers.y.name"), "y");
	protected final SupplierStorage<Double> z = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.vec3dFromCoordinates.subSuppliers.z.name"), "z");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(x, y, z);
	
	public Vec3dFromCoordinates(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3d>
	compileNotNull(CompileEnvironment environment) {
		var compiledDouble1Supplier = compileCheckedDouble(x.get(), environment);
		var compiledDouble2Supplier = compileCheckedDouble(y.get(), environment);
		var compiledDouble3Supplier = compileCheckedDouble(z.get(), environment);
		return map->new Vec3d(
			compiledDouble1Supplier.scriptApplyAsDouble(map),
			compiledDouble2Supplier.scriptApplyAsDouble(map),
			compiledDouble3Supplier.scriptApplyAsDouble(map)
		);
	}
}
