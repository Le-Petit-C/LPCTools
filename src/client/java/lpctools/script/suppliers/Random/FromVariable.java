package lpctools.script.suppliers.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.*;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class FromVariable<T> extends AbstractScript implements IRandomSupplier<T> {
	private @NotNull String variableName = "var";
	protected @Nullable WidthAutoAdjustTextField textField;
	public final Class<T> targetClass;
	
	public FromVariable(IScriptWithSubScript parent, Class<T> targetClass) {
		super(parent);
		this.targetClass = targetClass;
	}
	
	public void setVariableName(@NotNull String variableName) {
		if(!this.variableName.equals(variableName)){
			this.variableName = variableName;
			if(textField != null) textField.setText(variableName);
		}
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, variableName, text->{
				setVariableName(text);
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return List.of(textField);
	}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Object>
	compileRandom(CompileEnvironment variableMap) {
		var variableRef = variableMap.getVariableReference(variableName);
		return variableRef::getValue;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(variableName);
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonPrimitive primitive)) {
			warnFailedLoadingConfig("FromVariable", element);
			return;
		}
		variableName = primitive.getAsString();
	}
	
	@Override public Class<? extends T> getSuppliedClass() {return targetClass;}
}
