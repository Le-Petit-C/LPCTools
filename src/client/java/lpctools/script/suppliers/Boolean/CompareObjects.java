package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Random.Null;
import lpctools.util.Signs;
import net.minecraft.text.Text;

public class CompareObjects extends AbstractSignResultSupplier<Signs.ObjectCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Object> object1 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.compareObjects.subSuppliers.object1.name"), "object1");
	protected final SupplierStorage<Object> object2 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.compareObjects.subSuppliers.object2.name"), "object2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object1, object2);
	
	public CompareObjects(IScriptWithSubScript parent) {super(parent, Signs.EQUALS, Signs.objectCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var object1Supplier = object1.get().compile(variableMap);
		var sign = compareSign;
		var object2Supplier = object2.get().compile(variableMap);
		return map->sign.compareObjects(object1Supplier.scriptApply(map), object2Supplier.scriptApply(map));
	}
}
