package lpctools.script.suppliers.Double;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantDouble extends AbstractScript implements IDoubleSupplier {
	private @NotNull Double value = 0.0;
	protected @Nullable WidthAutoAdjustTextField textField = null;
	public ConstantDouble(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Double>
	compile(CompileEnvironment variableMap) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	public void setDoubleValue(@NotNull Double value){
		if(!this.value.equals(value)){
			this.value = value;
			if(textField != null) textField.setText(value.toString());
		}
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value);}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()){
			warnFailedLoadingConfig("ConstantDouble", element);
			return;
		}
		setDoubleValue(primitive.getAsDouble());
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getTextField());}
	
	private @NotNull WidthAutoAdjustTextField getTextField(){
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, value.toString(), text->{
				try {value = Double.valueOf(text);
				} catch (NumberFormatException ignored) {}
				textField.setText(value.toString());
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return textField;
	}
}
