package lpctools.script.suppliers.String;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ObjectToString extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IStringSupplier {
	
	protected final SupplierStorage<Object> object = ofStorage(Object.class,
		Text.translatable("lpctools.script.suppliers.string.objectToString.subSuppliers.object.name"), "object");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object);
	
	public ObjectToString(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<String>
	compileNotNull(CompileEnvironment environment) {
		var compiledObjectSupplier = object.get().compileCheckedNotNull(environment);
		return map->compiledObjectSupplier.scriptApply(map).toString();
	}
}
