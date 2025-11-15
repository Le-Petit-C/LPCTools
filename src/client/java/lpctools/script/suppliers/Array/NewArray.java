package lpctools.script.suppliers.Array;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NewArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IArraySupplier {
	protected final SupplierStorage<Integer> size = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.array.newArray.subSuppliers.size.name"), "size");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(size);
	
	public NewArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Object[]>
	compileNotNull(CompileEnvironment environment) {
		var compiledSizeSupplier = size.get().compileCheckedNotNull(environment);
		return map->new Object[compiledSizeSupplier.scriptApply(map)];
	}
}
