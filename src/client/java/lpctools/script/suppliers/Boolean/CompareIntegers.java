package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.text.Text;

public class CompareIntegers extends AbstractOperatorResultSupplier<Operators.IntegerCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareIntegers.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.boolean.compareIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public CompareIntegers(IScriptWithSubScript parent) {super(parent, Operators.EQUALS, Operators.integerCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var integer1Supplier = compileCheckedInteger(integer1.get(), environment);
		var sign = operatorSign;
		var integer2Supplier = compileCheckedInteger(integer2.get(), environment);
		return map->sign.compareIntegers(integer1Supplier.scriptApplyAsInt(map), integer2Supplier.scriptApplyAsInt(map));
	}
}
