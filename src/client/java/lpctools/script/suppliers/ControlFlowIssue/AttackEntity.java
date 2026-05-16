package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class AttackEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.attackEntity.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity);
	
	public AttackEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		return map->{
			var mc = Minecraft.getInstance();
			var itm = mc.gameMode;
			var player = mc.player;
			if(itm != null && player != null)
				itm.attack(player, compiledEntitySupplier.scriptApply(map));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
