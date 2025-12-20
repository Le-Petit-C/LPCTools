package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import org.jetbrains.annotations.NotNull;

public class DoubleConstant extends AbstractSignResultSupplier<Functions.DoubleConstant> implements IDoubleSupplier {
	public DoubleConstant(IScriptWithSubScript parent) {super(parent, Functions.PHI, Functions.doubleConstantInfo, 0);}
	
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		double val = compareSign.getDouble();
		return map->val;
	}
}
