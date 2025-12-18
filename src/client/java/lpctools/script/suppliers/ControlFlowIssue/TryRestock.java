package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.util.HandRestock;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TryRestock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Item> targetItem = ofStorage(Item.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.tryRestock.subSuppliers.targetItem.name"), "targetItem");
	protected final SupplierStorage<Boolean> offhand = ofStorage(Boolean.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.tryRestock.subSuppliers.offhand.name"), "offhand");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(targetItem, offhand);
	
	public TryRestock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledItemSupplier = targetItem.get().compileCheckedNotNull(environment);
		var compiledBooleanSupplier = offhand.get().compileCheckedNotNull(environment);
		return map->{
			Item target = compiledItemSupplier.scriptApply(map);
			boolean useOffhand = compiledBooleanSupplier.scriptApply(map);
			HandRestock.restock(stack->stack.getItem() == target, useOffhand ? -1 : 0);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
