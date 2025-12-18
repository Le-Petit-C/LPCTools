package lpctools.script.suppliers.BlockState;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BlockDefaultState extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockStateSupplier {
	protected final SupplierStorage<Block> block = ofStorage(Block.class,
		Text.translatable("lpctools.script.suppliers.blockState.blockDefaultState.subSuppliers.block.name"), "block");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(block);
	
	public BlockDefaultState(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockState>
	compileNotNull(CompileEnvironment environment) {
		var blockSupplier = block.get().compileCheckedNotNull(environment);
		return map->blockSupplier.scriptApply(map).getDefaultState();
	}
}
