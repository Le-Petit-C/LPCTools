package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class EntityInteractionRange extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IDoubleSupplier {
	protected final SupplierStorage<Player> player = ofStorage(Player.class,
		Component.translatable("lpctools.script.suppliers.double.entityInteractionRange.subSuppliers.player.name"), "player");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(player);
	
	public EntityInteractionRange(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		var compiledPlayerSupplier = player.get().compileCheckedNotNull(environment);
		return map->compiledPlayerSupplier.scriptApply(map).entityInteractionRange();
	}
}
