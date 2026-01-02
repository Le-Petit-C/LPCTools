package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.Operators;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerFunction extends AbstractOperatorResultSupplier<Operators.IntegerFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.integer.integerFunction.subSuppliers.integer.name"), "integer");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer);
	
	public IntegerFunction(IScriptWithSubScript parent) {super(parent, Operators.NEGATIVE, Operators.integerFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var integerSupplier = compileCheckedInteger(integer.get(), environment);
		return map->sign.applyInteger(integerSupplier.scriptApplyAsInt(map));
	}
}
