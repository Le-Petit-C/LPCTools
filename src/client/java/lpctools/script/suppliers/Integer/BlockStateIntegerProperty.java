package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class BlockStateIntegerProperty extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIntegerSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Component.translatable("lpctools.script.suppliers.integer.blockStateIntegerProperty.subSuppliers.blockState.name"), "blockState");
	protected final SupplierStorage<IntegerProperty> property = ofStorage(IntegerProperty.class,
		Component.translatable("lpctools.script.suppliers.integer.blockStateIntegerProperty.subSuppliers.property.name"), "property");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState, property);
	
	public BlockStateIntegerProperty(IScriptWithSubScript parent) {
		super(parent);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		var propertySupplier = property.get().compileCheckedNotNull(environment);
		return map->{
			try{
				return blockStateSupplier.scriptApply(map).getValue(propertySupplier.scriptApply(map));
			} catch (IllegalArgumentException e){
				throw ScriptRuntimeException.illegalArgument(this, e.getMessage());
			}
		};
	}
}
