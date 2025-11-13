package lpctools.script.suppliers.String;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantString extends AbstractScript implements IStringSupplier {
	private @NotNull String value = "";
	protected @Nullable WidthAutoAdjustTextField textField;
	public ConstantString(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNotNullSupplier<String>
	compileNotNull(CompileEnvironment environment) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	public void setStringValue(String value){
		if(!this.value.equals(value)){
			this.value = value;
			if(textField != null) textField.setText(String.valueOf(value));
		}
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value);}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive)){
			warnFailedLoadingConfig("ConstantString", element);
			return;
		}
		setStringValue(primitive.getAsString());
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getTextField());}
	
	private @NotNull WidthAutoAdjustTextField getTextField(){
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, value, text->{
					value = text;
					applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return textField;
	}
}
