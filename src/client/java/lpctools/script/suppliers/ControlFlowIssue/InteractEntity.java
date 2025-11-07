package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Boolean.ConstantBoolean;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public class InteractEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class, new ConstantBoolean(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactEntity.subSuppliers.useOffhand.name"), "useOffhand");
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactEntity.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(useOffhand, entity);
	
	public InteractEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledUseOffhandSupplier = useOffhand.get().compile(variableMap);
		var compiledEntitySupplier = entity.get().compile(variableMap);
		return map->{
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			var useOffhand = compiledUseOffhandSupplier.scriptApply(map);
			if(useOffhand == null) throw ScriptRuntimeException.nullPointer(this);
			var entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			itm.interactEntity(player, entity, useOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
