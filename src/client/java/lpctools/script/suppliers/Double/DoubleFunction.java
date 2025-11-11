package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoubleFunction extends AbstractSignResultSupplier<Functions.DoubleFunction> implements IDoubleSupplier {
	protected final SupplierStorage<Double> _double = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleFunction.subSuppliers.double.name"), "double");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_double);
	
	public DoubleFunction(IScriptWithSubScript parent) {super(parent, Functions.NEGATIVE, Functions.doubleFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Double>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var doubleSupplier = _double.get().compile(variableMap);
		return map->{
			var _double = doubleSupplier.scriptApply(map);
			if(_double == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.applyDouble(_double);
		};
	}
}
