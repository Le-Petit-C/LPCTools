package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;

public class NotNull extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Object> objectStorage = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.notNull.subSuppliers.object.name"), "objectStorage");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(objectStorage);
	
	public NotNull(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var compiledSupplier = objectStorage.get().compile(variableMap);
		return map->compiledSupplier.scriptApply(map) != null;
	}
}
