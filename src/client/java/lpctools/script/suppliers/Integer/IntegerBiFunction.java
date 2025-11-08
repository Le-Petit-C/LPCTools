package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Signs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerBiFunction extends AbstractSignResultSupplier<Signs.IntegerBiFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerBiFunction.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerBiFunction.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public IntegerBiFunction(IScriptWithSubScript parent) {super(parent, Signs.GCD, Signs.integerBiFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var integer1Supplier = integer1.get().compile(variableMap);
		var integer2Supplier = integer2.get().compile(variableMap);
		return map->{
			var integer1 = integer1Supplier.scriptApply(map);
			if(integer1 == null) throw ScriptRuntimeException.nullPointer(this);
			var integer2 = integer2Supplier.scriptApply(map);
			if(integer2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.apply2Integers(integer1, integer2);
		};
	}
}
