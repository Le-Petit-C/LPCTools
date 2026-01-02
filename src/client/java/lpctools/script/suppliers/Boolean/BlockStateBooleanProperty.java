package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BlockStateBooleanProperty extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final AbstractSupplierWithTypeDeterminedSubSuppliers.SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.boolean.blockStateBooleanProperty.subSuppliers.blockState.name"), "blockState");
	protected final AbstractSupplierWithTypeDeterminedSubSuppliers.SupplierStorage<BooleanProperty> property = ofStorage(BooleanProperty.class,
		Text.translatable("lpctools.script.suppliers.boolean.blockStateBooleanProperty.subSuppliers.property.name"), "property");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState, property);
	
	public BlockStateBooleanProperty(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		var propertySupplier = property.get().compileCheckedNotNull(environment);
		return map->blockStateSupplier.scriptApply(map).get(propertySupplier.scriptApply(map));
	}
}
