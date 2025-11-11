package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class EntityBlockPos extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockPosSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.entityBlockPos.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity);
	
	public EntityBlockPos(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = entity.get().compile(variableMap);
		return map->{
			Entity entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			return entity.getBlockPos();
		};
	}
}
