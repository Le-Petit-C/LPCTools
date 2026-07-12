package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DoubleFromVec3d extends AbstractOperatorResultSupplier<Operators.DoubleFromVec3dFunc> implements IDoubleSupplier {
	protected final SupplierStorage<Vec3> vec = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.double.doubleFromVec3d.subSuppliers.vec.name"), "vec");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec);
	
	public DoubleFromVec3d(IScriptWithSubScript parent) {super(parent, Operators.COORDINATE_X, Operators.doubleFromVec3dFuncInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var vecSupplier = vec.get().compileCheckedNotNull(environment);
		var sign = operatorSign;
		return map->sign.doubleFromVec3d(vecSupplier.scriptApply(map));
	}
}
