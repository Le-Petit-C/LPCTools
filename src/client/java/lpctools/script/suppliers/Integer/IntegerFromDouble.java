package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Double.ConstantDouble;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerFromDouble extends AbstractSignResultSupplier<Functions.Double2IntFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Double> _double = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerFromDouble.subSuppliers.double.name"), "double");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_double);
	
	public IntegerFromDouble(IScriptWithSubScript parent) {super(parent, Functions.FLOOR, Functions.double2IntFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var doubleSupplier = _double.get().compile(variableMap);
		return map->{
			var _double = doubleSupplier.scriptApply(map);
			if(_double == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.intFromDouble(_double);
		};
	}
}
