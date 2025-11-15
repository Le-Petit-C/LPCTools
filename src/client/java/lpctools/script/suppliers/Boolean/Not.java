package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;

public class Not extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Boolean> _boolean = ofStorage(Boolean.class,
		Text.translatable("lpctools.script.suppliers.Boolean.not.subSuppliers.boolean.name"), "boolean");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_boolean);
	
	public Not(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var booleanSupplier = compileCheckedBoolean(_boolean.get(), environment);
		return map->!booleanSupplier.scriptApplyAsBoolean(map);
	}
}
