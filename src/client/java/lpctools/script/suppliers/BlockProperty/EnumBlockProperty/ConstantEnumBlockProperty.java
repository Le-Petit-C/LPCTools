package lpctools.script.suppliers.BlockProperty.EnumBlockProperty;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import net.minecraft.state.property.EnumProperty;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockPropertyOperators.*;

public class ConstantEnumBlockProperty extends AbstractOperatorResultSupplier<EnumPropertyOperator> implements IEnumBlockPropertySupplier {
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	public ConstantEnumBlockProperty(IScriptWithSubScript parent) {
		super(parent, EnumPropertyOperator.propertyGetters.getDefault(), EnumPropertyOperator.propertyGetters, 0);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@SuppressWarnings("rawtypes")
	@Override public @NotNull ScriptNotNullSupplier<EnumProperty>
	compileNotNull(CompileEnvironment environment) {
		var sign = operatorSign;
		return map->sign.getProperty();
	}
}
