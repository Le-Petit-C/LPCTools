package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class DoubleFromVec3d extends AbstractSignResultSupplier<Functions.DoubleFromVec3dFunc> implements IDoubleSupplier {
	protected final SupplierStorage<Vec3d> vec = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.Double.doubleFromVec3d.subSuppliers.vec.name"), "vec");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec);
	
	public DoubleFromVec3d(IScriptWithSubScript parent) {super(parent, Functions.COORDINATE_X, Functions.doubleFromVec3dFuncInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var vecSupplier = vec.get().compileCheckedNotNull(environment);
		var sign = compareSign;
		return map->sign.doubleFromVec3d(vecSupplier.scriptApply(map));
	}
}
