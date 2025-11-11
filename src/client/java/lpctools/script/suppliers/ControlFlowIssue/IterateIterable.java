package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNotNullFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Iterable.ObjectIterable;
import lpctools.script.suppliers.Random.Null;
import lpctools.script.suppliers.ScriptSupplierLake;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class IterateIterable extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	private @NotNull String variableName = "var";
	protected @Nullable WidthAutoAdjustTextField textField;
	protected final SupplierStorage<ObjectIterable> iterable = ofStorage(ObjectIterable.class, new Null<>(this, ObjectIterable.class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.iterateIterable.subSuppliers.iterable.name"), iterableJsonKey);
	protected final RunMultiple loopBody = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.iterateIterable.loopBody.name"));
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(iterable);
	
	public static final String variableNameJsonKey = "variableName";
	public static final String iterableJsonKey = "iterable";
	public static final String loopBodyJsonKey = "loopBody";
	
	public IterateIterable(IScriptWithSubScript parent) {super(parent);}
	
	public void setVariableName(@NotNull String variableName) {
		if(!this.variableName.equals(variableName)){
			this.variableName = variableName;
			if(textField != null) textField.setText(variableName);
		}
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, variableName, text->{
				setVariableName(text);
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		res.add(textField);
		return super.buildWidgets(res);
	}
	
	@Override public @NotNull ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compileNotNull(CompileEnvironment variableMap) {
		var compiledIterableSupplier = iterable.get().compileCheckedNotNull(variableMap);
		var compiledLoopBody = loopBody.compileCheckedNotNull(variableMap);
		var variableRef = variableMap.getVariableReference(variableName);
		return map->{
			var iterable = compiledIterableSupplier.scriptApply(map);
			for(var v : iterable){
				variableRef.setValue(map, v);
				var issue = compiledLoopBody.scriptApply(map);
				if(issue.shouldBreak) return issue.applied();
			}
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.addProperty(variableNameJsonKey, variableName);
		iterable.getAsSubJsonElement(res);
		res.add(loopBodyJsonKey, loopBody.getAsJsonElement());
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("IterateIterable", element);
			return;
		}
		if(object.get(variableNameJsonKey) instanceof JsonElement varNameElement){
			if(varNameElement instanceof JsonPrimitive primitive)
				variableName = primitive.getAsString();
			else warnFailedLoadingConfig("IterateIterable.variableName", varNameElement);
		}
		ScriptSupplierLake.loadSupplierOrWarn(object.get(iterableJsonKey), ObjectIterable.class, this, v->iterable.set(v), "IterateIterable.value");
		loopBody.setValueFromJsonElement(object.get(loopBodyJsonKey));
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {return List.of(iterable.get(), loopBody);}
}
