package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Signs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerFunction extends AbstractSignResultSupplier<Signs.IntegerFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerFunction.subSuppliers.integer.name"), "integer");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer);
	
	public IntegerFunction(IScriptWithSubScript parent) {super(parent, Signs.NEGATIVE, Signs.integerFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var integerSupplier = integer.get().compile(variableMap);
		return map->{
			var integer = integerSupplier.scriptApply(map);
			if(integer == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.applyInteger(integer);
		};
	}
}
