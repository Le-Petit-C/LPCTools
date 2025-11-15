package lpctools.script.suppliers.Item;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class StackItem extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemSupplier {
	protected final SupplierStorage<ItemStack> stack = ofStorage(ItemStack.class,
		Text.translatable("lpctools.script.suppliers.Item.stackItem.subSuppliers.stack.name"), "stack");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(stack);
	
	public StackItem(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Item>
	compileNotNull(CompileEnvironment environment) {
		var compiledStackSupplier = stack.get().compileCheckedNotNull(environment);
		return map->compiledStackSupplier.scriptApply(map).getItem();
	}
}
