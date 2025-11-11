package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
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
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {
		var integer1Supplier = integer1.get().compile(variableMap);
		var sign = compareSign;
		var integer2Supplier = integer2.get().compile(variableMap);
		return map->{
			var integer1 = integer1Supplier.scriptApply(map);
			if(integer1 == null) throw ScriptRuntimeException.nullPointer(this);
			var integer2 = integer2Supplier.scriptApply(map);
			if(integer2 == null) throw ScriptRuntimeException.nullPointer(this);
			try{
				return sign.calculateIntegers(integer1, integer2);
			} catch (ArithmeticException e){
				throw ScriptRuntimeException.mathProblem(this);
			}
		};
	}
}
