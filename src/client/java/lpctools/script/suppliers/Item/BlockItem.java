package lpctools.script.suppliers.Item;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BlockItem extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemSupplier {
	protected final SupplierStorage<Block> block = ofStorage(Block.class,
		Text.translatable("lpctools.script.suppliers.item.blockItem.subSuppliers.block.name"), "block");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(block);
	
	public BlockItem(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Item>
	compileNotNull(CompileEnvironment environment) {
		var compiledBlockSupplier = block.get().compileCheckedNotNull(environment);
		return map->compiledBlockSupplier.scriptApply(map).asItem();
	}
}
