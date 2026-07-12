package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class InteractEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactEntity.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactEntity.subSuppliers.useOffhand.name"), "useOffhand");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity, useOffhand);
	
	public InteractEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		var compiledUseOffhandSupplier = useOffhand.get().compileCheckedNotNull(environment);
		return map->{
			var mc = Minecraft.getInstance();
			var itm = mc.gameMode;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			itm.interact(player, compiledEntitySupplier.scriptApply(map), compiledUseOffhandSupplier.scriptApply(map) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
