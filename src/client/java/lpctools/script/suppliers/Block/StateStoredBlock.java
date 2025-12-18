package lpctools.script.suppliers.Block;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class StateStoredBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.block.stateStoredBlock.subSuppliers.block.name"), "blockState");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState);
	
	public StateStoredBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Block>
	compileNotNull(CompileEnvironment environment) {
		var blockSupplier = blockState.get().compileCheckedNotNull(environment);
		return map->blockSupplier.scriptApply(map).getBlock();
	}
}
