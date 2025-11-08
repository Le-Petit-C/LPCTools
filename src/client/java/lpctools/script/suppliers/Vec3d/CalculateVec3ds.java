package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Signs;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CalculateVec3ds extends AbstractSignResultSupplier<Signs.Vec3dCalculateSign> implements IVec3dSupplier {
	protected final SupplierStorage<Vec3d> vec1 = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.calculateVec3ds.subSuppliers.vec1.name"), "vec1");
	protected final SupplierStorage<Vec3d> vec2 = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.calculateVec3ds.subSuppliers.vec2.name"), "vec2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec1, vec2);
	
	public CalculateVec3ds(IScriptWithSubScript parent) {super(parent, Signs.ADD, Signs.vec3dCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Vec3d>
	compile(CompileEnvironment variableMap) {
		var vec1Supplier = vec1.get().compile(variableMap);
		var sign = compareSign;
		var vec2Supplier = vec2.get().compile(variableMap);
		return map->{
			var vec1 = vec1Supplier.scriptApply(map);
			if(vec1 == null) throw ScriptRuntimeException.nullPointer(this);
			var vec2 = vec2Supplier.scriptApply(map);
			if(vec2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.calculateVec3ds(vec1, vec2);
		};
	}
}
