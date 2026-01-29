package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.text.Text;

public class CompareObjects extends AbstractOperatorResultSupplier<Operators.ObjectCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Object> object1 = ofStorage(Object.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareObjects.subSuppliers.object1.name"), "object1");
	protected final SupplierStorage<Object> object2 = ofStorage(Object.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareObjects.subSuppliers.object2.name"), "object2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object1, object2);
	
	public CompareObjects(IScriptWithSubScript parent) {super(parent, Operators.EQUALS, Operators.objectCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var object1Supplier = object1.get().compile(environment);
		var sign = operatorSign;
		var object2Supplier = object2.get().compile(environment);
		return map->sign.compareObjects(object1Supplier.scriptApply(map), object2Supplier.scriptApply(map));
	}
}
