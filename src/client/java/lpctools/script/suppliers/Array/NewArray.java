package lpctools.script.suppliers.Array;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NewArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IArraySupplier {
	protected final SupplierStorage<Integer> size = ofStorage(Integer.class, new Null<>(this, Integer.class),
		Text.translatable("lpctools.script.suppliers.Array.newArray.subSuppliers.size.name"), "size");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(size);
	
	public NewArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Object[]>
	compile(CompileEnvironment variableMap) {
		var compiledSizeSupplier = size.get().compile(variableMap);
		return map->{
			Integer size = compiledSizeSupplier.scriptApply(map);
			if(size == null) throw ScriptRuntimeException.nullPointer(this);
			return new Object[size];
		};
	}
}
