package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class SetVariable extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	private @NotNull String variableName = "var";
	protected @Nullable WidthAutoAdjustTextField textField;
	protected final SupplierStorage<Object> value = ofStorage(Object.class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setVariable.subSuppliers.value.name"), valueJsonKey);
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(value);
	public static final String variableNameJsonKey = "variableName";
	public static final String valueJsonKey = "value";
	
	public SetVariable(IScriptWithSubScript parent) {super(parent);}
	
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
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = value.get().compile(environment);
		var variableRef = environment.getVariableReference(variableName);
		return map->{
			Object object = compiledEntitySupplier.scriptApply(map);
			variableRef.setValue(map, object);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.addProperty(variableNameJsonKey, variableName);
		value.getAsSubJsonElement(res);
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
		value.setValueFromJsonElement(object.get(valueJsonKey));
	}
}
