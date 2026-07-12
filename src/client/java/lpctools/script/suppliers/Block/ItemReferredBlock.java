package lpctools.script.suppliers.Block;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class ItemReferredBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockSupplier {
	protected final SupplierStorage<Item> item = ofStorage(Item.class,
		Component.translatable("lpctools.script.suppliers.block.itemReferredBlock.subSuppliers.item.name"), "item");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(item);
	
	public ItemReferredBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Block>
	compileNotNull(CompileEnvironment environment) {
		var itemSupplier = item.get().compileCheckedNotNull(environment);
		return map->itemSupplier.scriptApply(map) instanceof BlockItem blockItem ?
			blockItem.getBlock().defaultBlockState().getBlock() : Blocks.AIR;
	}
}
