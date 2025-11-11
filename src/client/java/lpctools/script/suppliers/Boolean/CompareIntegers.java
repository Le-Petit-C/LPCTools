package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Integer.ConstantInteger;
import lpctools.util.Functions;
import net.minecraft.text.Text;

public class CompareIntegers extends AbstractSignResultSupplier<Functions.IntegerCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public CompareIntegers(IScriptWithSubScript parent) {super(parent, Functions.EQUALS, Functions.integerCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var integer1Supplier = integer1.get().compile(variableMap);
		var sign = compareSign;
		var integer2Supplier = integer2.get().compile(variableMap);
		return map->{
			var integer1 = integer1Supplier.scriptApply(map);
			if(integer1 == null) throw ScriptRuntimeException.nullPointer(this);
			var integer2 = integer2Supplier.scriptApply(map);
			if(integer2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.compareIntegers(integer1, integer2);
		};
	}
}
