package lpctools.script.suppliers.BlockProperty.IntegerBlockProperty.EnumBlockProperty;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import net.minecraft.state.property.IntProperty;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockPropertyOperators.*;

public class ConstantIntegerBlockProperty extends AbstractOperatorResultSupplier<IntegerPropertyOperator> implements IIntegerBlockPropertySupplier {
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	public ConstantIntegerBlockProperty(IScriptWithSubScript parent) {
		super(parent, IntegerPropertyOperator.propertyGetters.getFirstProperty(), IntegerPropertyOperator.propertyGetters, 0);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<IntProperty>
	compileNotNull(CompileEnvironment environment) {
		var sign = compareSign;
		return map->sign.getProperty();
	}
}
