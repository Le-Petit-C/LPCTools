package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerFromDouble extends AbstractSignResultSupplier<Functions.Double2IntFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Double> _double = ofStorage(Double.class,
		Text.translatable("lpctools.script.suppliers.integer.integerFromDouble.subSuppliers.double.name"), "double");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_double);
	
	public IntegerFromDouble(IScriptWithSubScript parent) {super(parent, Functions.FLOOR, Functions.double2IntFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var doubleSupplier = compileCheckedDouble(_double.get(), environment);
		return map->sign.intFromDouble(doubleSupplier.scriptApplyAsDouble(map));
	}
}
