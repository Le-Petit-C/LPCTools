package lpctools.script.suppliers.Double;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantDouble extends AbstractScript implements IDoubleSupplier {
	private double value = 0.0;
	protected @Nullable WidthAutoAdjustTextField textField = null;
	public ConstantDouble(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	public void setDoubleValue(double value){
		if(this.value != value){
			this.value = value;
			if(textField != null) textField.setText(String.valueOf(value));
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
				getDisplayWidget(), 100, String.valueOf(value), text->{
				try {value = Double.parseDouble(text);
				} catch (NumberFormatException ignored) {}
				textField.setText(String.valueOf(value));
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return textField;
	}
}
