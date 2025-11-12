package lpctools.script.suppliers.Type;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptType;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;

public class ObjectType extends AbstractSupplierWithTypeDeterminedSubSuppliers implements ITypeSupplier {
	protected final SupplierStorage<Object> object = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.notNull.subSuppliers.object.name"), "object");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object);
	
	public ObjectType(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNotNullSupplier<ScriptType>
	compileNotNull(CompileEnvironment environment) {
		var compiledObjectSupplier = object.get().compileCheckedNotNull(environment);
		return map->ScriptType.getType(compiledObjectSupplier.scriptApply(map));
	}
}
