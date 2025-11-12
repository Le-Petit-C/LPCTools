package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class AttackEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.attackEntity.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity);
	
	public AttackEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		return map->{
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			itm.attackEntity(player, compiledEntitySupplier.scriptApply(map));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
