package lpctools.script.suppliers.Item;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.item.Item;

public interface IItemSupplier extends IScriptSupplier<Item> {
	@Override default Class<? extends Item> getSuppliedClass(){return Item.class;}
}
