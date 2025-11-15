package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class EntityInteractionRange extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IDoubleSupplier {
	protected final SupplierStorage<PlayerEntity> player = ofStorage(PlayerEntity.class,
		Text.translatable("lpctools.script.suppliers.double.entityInteractionRange.subSuppliers.player.name"), "player");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(player);
	
	public EntityInteractionRange(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var compiledPlayerSupplier = player.get().compileCheckedNotNull(environment);
		return map->compiledPlayerSupplier.scriptApply(map).getEntityInteractionRange();
	}
}
