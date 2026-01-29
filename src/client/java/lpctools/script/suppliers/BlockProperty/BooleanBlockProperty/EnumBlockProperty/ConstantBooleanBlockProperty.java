package lpctools.script.suppliers.BlockProperty.BooleanBlockProperty.EnumBlockProperty;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import net.minecraft.state.property.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockPropertyOperators.*;

public class ConstantBooleanBlockProperty extends AbstractOperatorResultSupplier<BooleanPropertyGetter> implements IBooleanBlockPropertySupplier {
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	public ConstantBooleanBlockProperty(IScriptWithSubScript parent) {
		super(parent, BooleanPropertyGetter.propertyGetters.getDefault(), BooleanPropertyGetter.propertyGetters, 0);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BooleanProperty>
	compileNotNull(CompileEnvironment environment) {
		var sign = operatorSign;
		return map->sign.getProperty();
	}
}
