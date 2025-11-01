package lpctools.script.suppliers.booleans;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.randoms.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class Equals extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	SupplierStorage<Object> object1Storage = ofStorage(new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.booleans.equals.subSuppliers.object1.name"));
	SupplierStorage<Object> object2Storage = ofStorage(new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.booleans.equals.subSuppliers.object2.name"));
	
	protected @Nullable List<SubSupplierEntry<?>> subSuppliers = null;
	
	public Equals(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected List<SubSupplierEntry<?>> getSubSuppliers() {
		if(subSuppliers == null){
			subSuppliers = List.of(
				new SubSupplierEntry<>(Object.class, () -> this.object1Storage),
				new SubSupplierEntry<>(Object.class, () -> this.object2Storage)
			);
		}
		return subSuppliers;
	}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var object1 = object1Storage.get().compile(variableMap);
		var object2 = object2Storage.get().compile(variableMap);
		return map->Objects.equals(object1.scriptApply(map), object2.scriptApply(map));
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add("object1", ScriptSupplierLake.getJsonEntryFromSupplier(object1Storage.get()));
		res.add("object2", ScriptSupplierLake.getJsonEntryFromSupplier(object2Storage.get()));
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig(getName(), element);
			return;
		}
		ScriptSupplierLake.loadSupplierOrWarn(object.get("object1"), Object.class, this, object1Storage::set, "Equals.object1");
		ScriptSupplierLake.loadSupplierOrWarn(object.get("object2"), Object.class, this, object2Storage::set, "Equals.object2");
	}
	
	@Override public @org.jetbrains.annotations.NotNull List<? extends IScript> getSubScripts() {
		return List.of(object1Storage.get());
	}
}
