package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoubleBiFunction extends AbstractSignResultSupplier<Functions.DoubleBiFunction> implements IDoubleSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleBiFunction.subSuppliers.double1.name"), "double1");
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleBiFunction.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public DoubleBiFunction(IScriptWithSubScript parent) {super(parent, Functions.POW, Functions.doubleBiFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var sign = compareSign;
		var double1Supplier = compileCheckedDouble(double1.get(), environment);
		var double2Supplier = compileCheckedDouble(double2.get(), environment);
		return map->sign.apply2Doubles(double1Supplier.scriptApplyAsDouble(map), double2Supplier.scriptApplyAsDouble(map));
	}
}
