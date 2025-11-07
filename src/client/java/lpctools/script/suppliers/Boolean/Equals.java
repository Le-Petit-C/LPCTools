package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;

import java.util.Objects;

public class Equals extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Object> object1 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.equals.subSuppliers.object1.name"), "object1");
	protected final SupplierStorage<Object> object2 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.equals.subSuppliers.object2.name"), "object1");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object1, object2);
	
	public Equals(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var object1Supplier = object1.get().compile(variableMap);
		var object2Supplier = object2.get().compile(variableMap);
		return map->Objects.equals(object1Supplier.scriptApply(map), object2Supplier.scriptApply(map));
	}
}
