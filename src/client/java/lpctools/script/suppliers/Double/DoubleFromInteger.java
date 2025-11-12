package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoubleFromInteger extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IDoubleSupplier {
	protected final SupplierStorage<Integer> integer = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Double.doubleFromInteger.subSuppliers.integer.name"), "integer");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer);
	
	public DoubleFromInteger(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var compiledIntegerSupplier = compileCheckedInteger(integer.get(), environment);
		return map->(double)compiledIntegerSupplier.scriptApplyAsInt(map);
	}
}
