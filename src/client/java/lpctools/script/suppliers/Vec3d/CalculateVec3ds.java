package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CalculateVec3ds extends AbstractOperatorResultSupplier<Operators.Vec3dCalculateSign> implements IVec3dSupplier {
	protected final SupplierStorage<Vec3> vec1 = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.vec3d.calculateVec3ds.subSuppliers.vec1.name"), "vec1");
	protected final SupplierStorage<Vec3> vec2 = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.vec3d.calculateVec3ds.subSuppliers.vec2.name"), "vec2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec1, vec2);
	
	public CalculateVec3ds(IScriptWithSubScript parent) {super(parent, Operators.ADD, Operators.vec3dCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3>
	compileNotNull(CompileEnvironment environment) {
		var vec1Supplier = vec1.get().compileCheckedNotNull(environment);
		var sign = operatorSign;
		var vec2Supplier = vec2.get().compileCheckedNotNull(environment);
		return map->sign.calculateVec3ds(vec1Supplier.scriptApply(map), vec2Supplier.scriptApply(map));
	}
}
