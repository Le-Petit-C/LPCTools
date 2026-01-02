package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.Operators;
import net.minecraft.text.Text;

public class CompareDoubles extends AbstractOperatorResultSupplier<Operators.DoubleCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareDoubles.subSuppliers.double1.name"), "double1");
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareDoubles.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public CompareDoubles(IScriptWithSubScript parent) {super(parent, Operators.EQUALS, Operators.doubleCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var double1Supplier = compileCheckedDouble(double1.get(), environment);
		var sign = compareSign;
		var double2Supplier = compileCheckedDouble(double2.get(), environment);
		return map->sign.compareDoubles(double1Supplier.scriptApplyAsDouble(map), double2Supplier.scriptApplyAsDouble(map));
	}
}
