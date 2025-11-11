package lpctools.script.suppliers.Item;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.IScriptSupplierNotNull;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class StackItem extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemSupplier, IScriptSupplierNotNull<Item> {
	protected final SupplierStorage<ItemStack> stack = ofStorage(ItemStack.class, new Null<>(this, ItemStack.class),
		Text.translatable("lpctools.script.suppliers.Item.stackItem.subSuppliers.stack.name"), "stack");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(stack);
	
	public StackItem(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, Item>
	compileNotNull(CompileEnvironment variableMap) {
		var compiledStackSupplier = stack.get().compileCheckedNotNull(variableMap);
		return map->compiledStackSupplier.scriptApply(map).getItem();
	}
}
