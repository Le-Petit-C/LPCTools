package lpctools.script.suppliers.ItemStack;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.item.ItemStack;

public interface IItemStackSupplier extends IScriptSupplier<ItemStack> {
	@Override default Class<? extends ItemStack> getSuppliedClass(){return ItemStack.class;}
}
