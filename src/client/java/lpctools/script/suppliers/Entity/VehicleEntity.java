package lpctools.script.suppliers.Entity;

import com.google.gson.JsonElement;
import lpctools.script.*;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VehicleEntity extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IEntitySupplier {
	
	protected final SupplierStorage<Entity> passenger = ofStorage(new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.entities.vehicleEntity.subSuppliers.passenger.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(Entity.class, passenger)
		.build();
	
	public VehicleEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Entity>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = passenger.get().compile(variableMap);
		return map->{
			Entity entity = compiledEntitySupplier.scriptApply(map);
			if(entity == null) throw ScriptRuntimeException.nullPointer(this);
			return entity.getVehicle();
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(passenger.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, Entity.class, this, res -> passenger.set(res), "VehicleEntity.passenger");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {return List.of(passenger.get());}
}
