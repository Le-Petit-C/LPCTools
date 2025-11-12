package lpctools.script.suppliers.ItemStack;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.item.ItemStack;

public interface IItemStackSupplier extends IScriptSupplierNotNull<ItemStack> {
	@Override default Class<? extends ItemStack> getSuppliedClass(){return ItemStack.class;}
}
