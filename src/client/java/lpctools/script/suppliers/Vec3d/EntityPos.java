package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class EntityPos extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.Vec3d.entityPos.subSuppliers.entity.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(entity, "entity")
		.build();
	
	public EntityPos(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Vec3d>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = entity.get().compile(variableMap);
		return map->{
			Entity entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			return entity.getPos();
		};
	}
}
