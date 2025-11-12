package lpctools.script.suppliers.Integer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantInteger extends AbstractScript implements IIntegerSupplier {
	private int value = 0;
	protected @Nullable WidthAutoAdjustTextField textField;
	public ConstantInteger(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	public void setIntegerValue(int value){
		if(this.value != value){
			this.value = value;
			if(textField != null) textField.setText(String.valueOf(value));
		}
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value);}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()){
			warnFailedLoadingConfig("ConstantInteger", element);
			return;
		}
		setIntegerValue(primitive.getAsInt());
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getTextField());}
	
	private @NotNull WidthAutoAdjustTextField getTextField(){
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, String.valueOf(value), text->{
				try {value = Integer.parseInt(text);
				} catch (NumberFormatException ignored) {}
				textField.setText(String.valueOf(value));
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return textField;
	}
}
