package lpctools.script.suppliers.Type;

import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.ScriptType;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjectType extends AbstractSupplierWithTypeDeterminedSubSuppliers implements ITypeSupplier {
	protected final SupplierStorage<Object> objectStorage = ofStorage(new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.notNull.subSuppliers.object.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(Object.class, objectStorage)
		.build();
	
	public ObjectType(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ScriptType>
	compile(CompileEnvironment variableMap) {
		var compiledSupplier = objectStorage.get().compile(variableMap);
		return map->ScriptType.getType(compiledSupplier.scriptApply(map));
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(objectStorage.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, Object.class, this, objectStorage::set, "ObjectType.object");
	}
	
	@Override public @org.jetbrains.annotations.NotNull List<? extends IScript> getSubScripts() {
		return List.of(objectStorage.get());
	}
}
