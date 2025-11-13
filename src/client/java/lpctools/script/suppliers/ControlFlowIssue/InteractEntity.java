package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Boolean.ConstantBoolean;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public class InteractEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactEntity.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class, new ConstantBoolean(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactEntity.subSuppliers.useOffhand.name"), "useOffhand");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity, useOffhand);
	
	public InteractEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		var compiledUseOffhandSupplier = useOffhand.get().compileCheckedNotNull(environment);
		return map->{
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			itm.interactEntity(player, compiledEntitySupplier.scriptApply(map), compiledUseOffhandSupplier.scriptApply(map) ? Hand.OFF_HAND : Hand.MAIN_HAND);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
