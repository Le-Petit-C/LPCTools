package lpctools.script.suppliers.Integer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantInteger extends AbstractScript implements IIntegerSupplier {
	Integer value = 0;
	private @Nullable WidthAutoAdjustTextField textField = null;
	public ConstantInteger(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {return map->value;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value);}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()){
			warnFailedLoadingConfig("ConstantInteger", element);
			return;
		}
		value = primitive.getAsInt();
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getTextField());}
	
	private @NotNull WidthAutoAdjustTextField getTextField(){
		if(textField == null){
			textField = new WidthAutoAdjustTextField(
				getDisplayWidget(), 100, text->{
				try {
					value = Integer.valueOf(text);
				} catch (NumberFormatException e) {
					textField.setText(value.toString());
				}
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
		return textField;
	}
}
