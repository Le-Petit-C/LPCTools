package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Vec3d.ConstantVec3d;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class DoubleFromVec3d extends AbstractSignResultSupplier<Functions.DoubleFromVec3dFunc> implements IDoubleSupplier {
	protected final SupplierStorage<Vec3d> vec = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleFromVec3d.subSuppliers.vec.name"), "vec");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec);
	
	public DoubleFromVec3d(IScriptWithSubScript parent) {super(parent, Functions.COORDINATE_X, Functions.doubleFromVec3dFuncInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Double>
	compile(CompileEnvironment variableMap) {
		var vecSupplier = vec.get().compile(variableMap);
		var sign = compareSign;
		return map->{
			var vec = vecSupplier.scriptApply(map);
			if(vec == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.doubleFromVec3d(vec);
		};
	}
}
