package lpctools.script.suppliers.Entity;

import lpctools.script.*;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class VehicleEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IEntitySupplier {
	
	protected final SupplierStorage<Entity> passenger = ofStorage(Entity.class,
		Text.translatable("lpctools.script.suppliers.Entity.vehicleEntity.subSuppliers.passenger.name"), "passenger");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(passenger);
	
	public VehicleEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableSupplier<Entity>
	compile(CompileEnvironment environment) {
		var compiledEntitySupplier = passenger.get().compileCheckedNotNull(environment);
		return map->compiledEntitySupplier.scriptApply(map).getVehicle();
	}
}
