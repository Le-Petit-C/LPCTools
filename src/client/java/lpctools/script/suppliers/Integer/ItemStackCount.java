package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackCount extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIntegerSupplier {
	protected final SupplierStorage<ItemStack> stack = ofStorage(ItemStack.class,
		Component.translatable("lpctools.script.suppliers.integer.itemStackCount.subSuppliers.stack.name"), "stack");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(stack);
	
	public ItemStackCount(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var compiledSupplier = stack.get().compileCheckedNotNull(environment);
		return map->compiledSupplier.scriptApply(map).getCount();
	}
}
