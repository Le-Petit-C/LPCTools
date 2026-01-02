package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.Operators;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CalculateIntegers extends AbstractOperatorResultSupplier<Operators.IntegerCalculateSign> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.integer.calculateIntegers.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.integer.calculateIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public CalculateIntegers(IScriptWithSubScript parent) {super(parent, Operators.ADD, Operators.integerCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var integer1Supplier = compileCheckedInteger(integer1.get(), environment);
		var sign = compareSign;
		var integer2Supplier = compileCheckedInteger(integer2.get(), environment);
		return map->{
			try{
				return sign.calculateIntegers(integer1Supplier.scriptApplyAsInt(map), integer2Supplier.scriptApplyAsInt(map));
			} catch (ArithmeticException e){
				throw ScriptRuntimeException.mathProblem(this);
			}
		};
	}
}
