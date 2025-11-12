package lpctools.script.suppliers.Item;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.item.Item;

public interface IItemSupplier extends IScriptSupplierNotNull<Item> {
	@Override default Class<? extends Item> getSuppliedClass(){return Item.class;}
}
