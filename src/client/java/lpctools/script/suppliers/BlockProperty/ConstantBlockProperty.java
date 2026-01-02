package lpctools.script.suppliers.BlockProperty;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockPropertyOperators.GenericPropertyOperator;

public class ConstantBlockProperty extends AbstractOperatorResultSupplier<GenericPropertyOperator<?>> implements IBlockPropertySupplier {
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	public ConstantBlockProperty(IScriptWithSubScript parent) {
		super(parent, GenericPropertyOperator.propertyGetters.getFirstProperty(), GenericPropertyOperator.propertyGetters, 0);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@SuppressWarnings("rawtypes")
	@Override public @NotNull ScriptNotNullSupplier<Property> compileNotNull(CompileEnvironment environment) {
		var sign = compareSign;
		return map -> sign.getProperty();
	}
}
