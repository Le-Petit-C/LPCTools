package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CalculateIntegers extends AbstractSignResultSupplier<Functions.IntegerCalculateSign> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.calculateIntegers.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.calculateIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public CalculateIntegers(IScriptWithSubScript parent) {super(parent, Functions.ADD, Functions.integerCalculateSignInfo, 1);}
	
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
