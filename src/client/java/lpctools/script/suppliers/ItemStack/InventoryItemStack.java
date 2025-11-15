package lpctools.script.suppliers.ItemStack;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class InventoryItemStack extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemStackSupplier{
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.ItemStack.inventoryItemStack.subSuppliers.index.name"), "index");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(index);
	
	public InventoryItemStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ItemStack>
	compileNotNull(CompileEnvironment environment) {
		var compiledIndexSupplier = compileCheckedInteger(index.get(), environment);
		return map->{
			if(MinecraftClient.getInstance().player instanceof ClientPlayerEntity player){
				int i = compiledIndexSupplier.scriptApplyAsInt(map);
				var inventory = player.getInventory();
				if(i >= 0 && i < inventory.size())
					return inventory.getStack(i);
			}
			return ItemStack.EMPTY;
		};
	}
}
