package lpctools.script.suppliers.booleans;

import com.google.gson.JsonElement;
import lpctools.script.CompileTimeVariableMap;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.randoms.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NotNull extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	SupplierStorage<Object> objectStorage = ofStorage(new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.booleans.notNull.subSuppliers.object.name"));
	
	protected @Nullable List<SubSupplierEntry<?>> subSuppliers = null;
	
	public NotNull(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected List<SubSupplierEntry<?>> getSubSuppliers() {
		if(subSuppliers == null){
			subSuppliers = List.of(
				new SubSupplierEntry<>(Object.class, () -> this.objectStorage)
			);
		}
		return subSuppliers;
	}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<RuntimeVariableMap, Boolean>
	compile(CompileTimeVariableMap variableMap) {
		var compiledSupplier = objectStorage.get().compile(variableMap);
		return map->compiledSupplier.scriptApply(map) != null;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(objectStorage.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, Object.class, this, objectStorage::set, "NotNull.object");
	}
	
	@Override public @org.jetbrains.annotations.NotNull List<? extends IScript> getSubScripts() {
		return List.of(objectStorage.get());
	}
}
