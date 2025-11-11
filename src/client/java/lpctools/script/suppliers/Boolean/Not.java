package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;

public class Not extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Boolean> _boolean = ofStorage(Boolean.class, new ConstantBoolean(this),
		Text.translatable("lpctools.script.suppliers.Boolean.not.subSuppliers.boolean.name"), "boolean");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(_boolean);
	
	public Not(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var booleanSupplier = _boolean.get().compile(variableMap);
		return map->{
			var _boolean = booleanSupplier.scriptApply(map);
			if(_boolean == null) throw ScriptRuntimeException.nullPointer(this);
			return !_boolean;
		};
	}
}
