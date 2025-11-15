package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SetArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.index.name"), "index");
	protected final SupplierStorage<Object> value = ofStorage(Object.class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.value.name"), "value");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array, index, value);
	
	public SetArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledArraySupplier = array.get().compileCheckedNotNull(environment);
		var compiledIndexSupplier = index.get().compileCheckedNotNull(environment);
		var compiledValueSupplier = value.get().compile(environment);
		return map->{
			Object value = compiledValueSupplier.scriptApply(map);
			Object[] array = compiledArraySupplier.scriptApply(map);
			int index = compiledIndexSupplier.scriptApply(map);
			if(index < 0 || index >= array.length)
				throw ScriptRuntimeException.indexOutOfBounds(this, index, array.length);
			array[index] = value;
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
