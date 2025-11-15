package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CalculateVec3ds extends AbstractSignResultSupplier<Functions.Vec3dCalculateSign> implements IVec3dSupplier {
	protected final SupplierStorage<Vec3d> vec1 = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.vec3d.calculateVec3ds.subSuppliers.vec1.name"), "vec1");
	protected final SupplierStorage<Vec3d> vec2 = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.vec3d.calculateVec3ds.subSuppliers.vec2.name"), "vec2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec1, vec2);
	
	public CalculateVec3ds(IScriptWithSubScript parent) {super(parent, Functions.ADD, Functions.vec3dCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3d>
	compileNotNull(CompileEnvironment environment) {
		var vec1Supplier = vec1.get().compileCheckedNotNull(environment);
		var sign = compareSign;
		var vec2Supplier = vec2.get().compileCheckedNotNull(environment);
		return map->sign.calculateVec3ds(vec1Supplier.scriptApply(map), vec2Supplier.scriptApply(map));
	}
}
