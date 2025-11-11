package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Double.ConstantDouble;
import lpctools.util.Functions;
import net.minecraft.text.Text;

public class CompareDoubles extends AbstractSignResultSupplier<Functions.DoubleCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareDoubles.subSuppliers.double1.name"), "double1");
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareDoubles.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public CompareDoubles(IScriptWithSubScript parent) {super(parent, Functions.EQUALS, Functions.doubleCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var double1Supplier = double1.get().compile(variableMap);
		var sign = compareSign;
		var double2Supplier = double2.get().compile(variableMap);
		return map->{
			var double1 = double1Supplier.scriptApply(map);
			if(double1 == null) throw ScriptRuntimeException.nullPointer(this);
			var double2 = double2Supplier.scriptApply(map);
			if(double2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.compareDoubles(double1, double2);
		};
	}
}
