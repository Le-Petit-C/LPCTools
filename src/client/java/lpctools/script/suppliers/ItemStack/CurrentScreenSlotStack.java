package lpctools.script.suppliers.ItemStack;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CurrentScreenSlotStack extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemStackSupplier{
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.ItemStack.currentScreenSlotStack.subSuppliers.index.name"), "index");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(index);
	
	public CurrentScreenSlotStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ItemStack>
	compileNotNull(CompileEnvironment environment) {
		var compiledIndexSupplier = compileCheckedInteger(index.get(), environment);
		return map->{
			if(MinecraftClient.getInstance().player instanceof ClientPlayerEntity player){
				var slots = player.currentScreenHandler.slots;
				int i = compiledIndexSupplier.scriptApplyAsInt(map);
				if(i >= 0 && i < slots.size())
					return slots.get(i).getStack();
			}
			return ItemStack.EMPTY;
		};
	}
}
