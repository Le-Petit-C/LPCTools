package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class EntityEyePos extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.entityEyePos.subSuppliers.entity.name"), "entity");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity);
	
	public EntityEyePos(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3d>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		return map->compiledEntitySupplier.scriptApply(map).getEyePos();
	}
}
