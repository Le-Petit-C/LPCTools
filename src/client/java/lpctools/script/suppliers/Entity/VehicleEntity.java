package lpctools.script.suppliers.Entity;

import lpctools.script.*;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class VehicleEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IEntitySupplier {
	
	protected final SupplierStorage<Entity> passenger = ofStorage(Entity.class, new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.Entity.vehicleEntity.subSuppliers.passenger.name"), "passenger");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(passenger);
	
	public VehicleEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Entity>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = passenger.get().compile(variableMap);
		return map->{
			Entity entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			return entity.getVehicle();
		};
	}
}
