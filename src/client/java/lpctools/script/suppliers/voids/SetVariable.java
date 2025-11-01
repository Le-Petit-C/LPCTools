package lpctools.script.suppliers.voids;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.ScriptFitTextField;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.randoms.Null;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class SetVariable extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVoidSupplier {
	
	private @NotNull String variableName = "var";
	SupplierStorage<Object> value = ofStorage(new Null<>(this, Entity.class),
		Text.translatable("lpctools.script.suppliers.voids.setVariable.subSuppliers.value.name"));
	protected @Nullable List<SubSupplierEntry<?>> subSuppliers = null;
	public static final String variableNameJsonKey = "variableName";
	public static final String valueJsonKey = "value";
	
	public SetVariable(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected List<SubSupplierEntry<?>> getSubSuppliers() {
		if(subSuppliers == null){
			subSuppliers = List.of(
				new SubSupplierEntry<>(Object.class, () -> value)
			);
		}
		return subSuppliers;
	}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		res.add(new ScriptFitTextField(
			getDisplayWidget(), 100, text->{
			variableName = text;
			applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
		}));
		return super.buildWidgets(res);
	}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Void>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = value.get().compile(variableMap);
		var variableRef = variableMap.getVariableReference(variableName);
		return map->{
			Object object = compiledEntitySupplier.scriptApply(map);
			variableRef.setValue(map, object);
			return null;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.addProperty(variableNameJsonKey, variableName);
		res.add(valueJsonKey, ScriptSupplierLake.getJsonEntryFromSupplier(value.get()));
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("SetVariable", element);
			return;
		}
		if(object.get(variableNameJsonKey) instanceof JsonElement varNameElement){
			if(varNameElement instanceof JsonPrimitive primitive)
				variableName = primitive.getAsString();
			else warnFailedLoadingConfig("SetVariable.variableName", varNameElement);
		}
		ScriptSupplierLake.loadSupplierOrWarn(object.get(valueJsonKey), Object.class, this, res -> value.set(res), "SetVariable.value");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(value.get());
	}
}
