package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
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
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Vec3d>
	compile(CompileEnvironment variableMap) {
		var compiledDouble1Supplier = x.get().compile(variableMap);
		var compiledDouble2Supplier = y.get().compile(variableMap);
		var compiledDouble3Supplier = z.get().compile(variableMap);
		return map->{
			var x = compiledDouble1Supplier.scriptApply(map);
			if(x == null) throw ScriptRuntimeException.nullPointer(this);
			var y = compiledDouble2Supplier.scriptApply(map);
			if(y == null) throw ScriptRuntimeException.nullPointer(this);
			var z = compiledDouble3Supplier.scriptApply(map);
			if(z == null) throw ScriptRuntimeException.nullPointer(this);
			return new Vec3d(x, y, z);
		};
	}
}
