package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemMaxStack extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIntegerSupplier {
	protected final SupplierStorage<Item> item = ofStorage(Item.class,
		Component.translatable("lpctools.script.suppliers.integer.itemMaxStack.subSuppliers.item.name"), "item");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(item);
	
	public ItemMaxStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var compiledSupplier = item.get().compileCheckedNotNull(environment);
		return map->compiledSupplier.scriptApply(map).getDefaultMaxStackSize();
	}
}
