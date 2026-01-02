package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DoBlockStateHasProperty extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.boolean.doBlockStateHasProperty.subSuppliers.blockState.name"), "blockState");
	@SuppressWarnings("rawtypes")
	protected final SupplierStorage<Property> property = ofStorage(Property.class,
		Text.translatable("lpctools.script.suppliers.boolean.doBlockStateHasProperty.subSuppliers.property.name"), "property");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState, property);
	
	public DoBlockStateHasProperty(IScriptWithSubScript parent) {
		super(parent);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		var propertySupplier = property.get().compileCheckedNotNull(environment);
		return map -> blockStateSupplier.scriptApply(map).contains(propertySupplier.scriptApply(map));
	}
}
