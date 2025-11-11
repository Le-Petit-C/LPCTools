package lpctools.script.suppliers.Double;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BlockInteractionRange extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IDoubleSupplier {
	protected final SupplierStorage<PlayerEntity> player = ofStorage(PlayerEntity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.Double.blockInteractionRange.subSuppliers.player.name"), "player");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(player);
	
	public BlockInteractionRange(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Double>
	compile(CompileEnvironment variableMap) {
		var compiledPlayerSupplier = player.get().compile(variableMap);
		return map->{
			PlayerEntity player = compiledPlayerSupplier.scriptApply(map);
			if(player == null) throw ScriptRuntimeException.nullPointer(this);
			return player.getBlockInteractionRange();
		};
	}
}
