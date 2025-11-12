package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerFunction extends AbstractSignResultSupplier<Functions.IntegerFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerFunction.subSuppliers.integer.name"), "integer");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer);
	
	public IntegerFunction(IScriptWithSubScript parent) {super(parent, Functions.NEGATIVE, Functions.integerFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var integerSupplier = compileCheckedInteger(integer.get(), environment);
		return map->sign.applyInteger(integerSupplier.scriptApplyAsInt(map));
	}
}
