package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CalculateDoubles extends AbstractSignResultSupplier<Functions.DoubleCalculateSign> implements IDoubleSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.calculateDoubles.subSuppliers.double1.name"), "double1");
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.calculateDoubles.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public CalculateDoubles(IScriptWithSubScript parent) {super(parent, Functions.ADD, Functions.doubleCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var double1Supplier = compileCheckedDouble(double1.get(), environment);
		var sign = compareSign;
		var double2Supplier = compileCheckedDouble(double2.get(), environment);
		return map->sign.calculateDoubles(double1Supplier.scriptApplyAsDouble(map), double2Supplier.scriptApplyAsDouble(map));
	}
}
