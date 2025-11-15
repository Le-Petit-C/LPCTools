package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class DoubleFromVec3ds extends AbstractSignResultSupplier<Functions.DoubleFromVec3dsSign> implements IDoubleSupplier {
	protected final SupplierStorage<Vec3d> vec1 = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.Double.doubleFromVec3ds.subSuppliers.vec1.name"), "vec1");
	protected final SupplierStorage<Vec3d> vec2 = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.Double.doubleFromVec3ds.subSuppliers.vec2.name"), "vec2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec1, vec2);
	
	public DoubleFromVec3ds(IScriptWithSubScript parent) {super(parent, Functions.DOT, Functions.doubleFromVec3dsSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var vec1Supplier = vec1.get().compileCheckedNotNull(environment);
		var sign = compareSign;
		var vec2Supplier = vec2.get().compileCheckedNotNull(environment);
		return map->sign.doubleFromVec3ds(vec1Supplier.scriptApply(map), vec2Supplier.scriptApply(map));
	}
}
