package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoubleFunction extends AbstractSignResultSupplier<Functions.DoubleFunction> implements IDoubleSupplier {
	protected final SupplierStorage<Double> _double = ofStorage(Double.class,
		Text.translatable("lpctools.script.suppliers.Double.doubleFunction.subSuppliers.double.name"), "double");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_double);
	
	public DoubleFunction(IScriptWithSubScript parent) {super(parent, Functions.NEGATIVE, Functions.doubleFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var sign = compareSign;
		var doubleSupplier = compileCheckedDouble(_double.get(), environment);
		return map->sign.applyDouble(doubleSupplier.scriptApplyAsDouble(map));
	}
}
