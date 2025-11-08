package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Signs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoubleBiFunction extends AbstractSignResultSupplier<Signs.DoubleBiFunction> implements IDoubleSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleBiFunction.subSuppliers.double1.name"), "double1");
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleBiFunction.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public DoubleBiFunction(IScriptWithSubScript parent) {super(parent, Signs.POW, Signs.doubleBiFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Double>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var double1Supplier = double1.get().compile(variableMap);
		var double2Supplier = double2.get().compile(variableMap);
		return map->{
			var double1 = double1Supplier.scriptApply(map);
			if(double1 == null) throw ScriptRuntimeException.nullPointer(this);
			var double2 = double2Supplier.scriptApply(map);
			if(double2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.apply2Doubles(double1, double2);
		};
	}
}
