package lpctools.script.suppliers.ControlFlow;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class AttackEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.attackEntity.subSuppliers.entity.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(entity, "entity")
		.build();
	
	public AttackEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = entity.get().compile(variableMap);
		return map->{
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			Entity entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			itm.attackEntity(player, entity);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
