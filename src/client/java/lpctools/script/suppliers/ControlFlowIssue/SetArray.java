package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SetArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class, new Null<>(this, Object[].class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.index.name"), "index");
	protected final SupplierStorage<Object> value = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.value.name"), "value");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array, index, value);
	
	public SetArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledArraySupplier = array.get().compile(variableMap);
		var compiledIndexSupplier = index.get().compile(variableMap);
		var compiledValueSupplier = value.get().compile(variableMap);
		return map->{
			Object value = compiledValueSupplier.scriptApply(map);
			Object[] array = compiledArraySupplier.scriptApply(map);
			if(array == null) throw ScriptRuntimeException.nullPointer(this);
			Integer index = compiledIndexSupplier.scriptApply(map);
			if(index == null) throw ScriptRuntimeException.nullPointer(this);
			if(index < 0 || index >= array.length)
				throw ScriptRuntimeException.indexOutOfBounds(this, index, array.length);
			array[index] = value;
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
