package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Integer.ConstantInteger;
import lpctools.util.Functions;
import net.minecraft.text.Text;

public class CompareIntegers extends AbstractSignResultSupplier<Functions.IntegerCompareSign> implements IBooleanSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public CompareIntegers(IScriptWithSubScript parent) {super(parent, Functions.EQUALS, Functions.integerCompareSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var integer1Supplier = compileCheckedInteger(integer1.get(), environment);
		var sign = compareSign;
		var integer2Supplier = compileCheckedInteger(integer2.get(), environment);
		return map->sign.compareIntegers(integer1Supplier.scriptApplyAsInt(map), integer2Supplier.scriptApplyAsInt(map));
	}
}
