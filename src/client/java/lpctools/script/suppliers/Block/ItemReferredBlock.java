package lpctools.script.suppliers.Block;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ItemReferredBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockSupplier {
	protected final SupplierStorage<Item> item = ofStorage(Item.class,
		Text.translatable("lpctools.script.suppliers.block.itemReferredBlock.subSuppliers.item.name"), "item");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(item);
	
	public ItemReferredBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Block>
	compileNotNull(CompileEnvironment environment) {
		var itemSupplier = item.get().compileCheckedNotNull(environment);
		return map->itemSupplier.scriptApply(map) instanceof BlockItem blockItem ?
			blockItem.getBlock().getDefaultState().getBlock() : Blocks.AIR;
	}
}
