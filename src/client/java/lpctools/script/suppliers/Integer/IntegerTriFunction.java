package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class IntegerTriFunction extends AbstractSignResultSupplier<Functions.IntegerTriFunction> implements IIntegerSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.Integer.integerTriFunction.subSuppliers.integer1.name"), "integer1");
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.Integer.integerTriFunction.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<Integer> integer3 = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.Integer.integerTriFunction.subSuppliers.integer3.name"), "integer3");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public IntegerTriFunction(IScriptWithSubScript parent) {super(parent, Functions.MOD_POW, Functions.integerTriFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var integer1Supplier = compileCheckedInteger(integer1.get(), environment);
		var integer2Supplier = compileCheckedInteger(integer2.get(), environment);
		var integer3Supplier = compileCheckedInteger(integer3.get(), environment);
		return map->sign.apply3Integers(integer1Supplier.scriptApplyAsInt(map), integer2Supplier.scriptApplyAsInt(map), integer3Supplier.scriptApplyAsInt(map));
	}
}
