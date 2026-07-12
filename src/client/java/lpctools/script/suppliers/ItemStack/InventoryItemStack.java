package lpctools.script.suppliers.ItemStack;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryItemStack extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemStackSupplier{
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class,
		Component.translatable("lpctools.script.suppliers.itemStack.inventoryItemStack.subSuppliers.index.name"), "index");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(index);
	
	public InventoryItemStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ItemStack>
	compileNotNull(CompileEnvironment environment) {
		var compiledIndexSupplier = compileCheckedInteger(index.get(), environment);
		return map->{
			if(Minecraft.getInstance().player instanceof LocalPlayer player){
				int i = compiledIndexSupplier.scriptApplyAsInt(map);
				var inventory = player.getInventory();
				if(i >= 0 && i < inventory.getContainerSize())
					return inventory.getItem(i);
			}
			return ItemStack.EMPTY;
		};
	}
}
