package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigValue;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniqueStringConfig extends LPCUniqueConfigBase implements IConfigValue {
	protected String stringValue;
	public final String defaultStringValue;
	public UniqueStringConfig(@NotNull ILPCConfigBase parent, @NotNull String nameKey, @Nullable String defaultStringValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		stringValue = this.defaultStringValue = defaultStringValue == null ? "" : defaultStringValue;
	}
	@Override public boolean isModified() {return !stringValue.equals(defaultStringValue);}
	@Override public void resetToDefault() {stringValue = defaultStringValue;}
	@Override public String getDefaultStringValue() {return defaultStringValue;}
	@Override public void setValueFromString(String s) {
		if(isModified(s)){
			stringValue = s;
			onValueChanged();
		}
	}
	@Override public boolean isModified(String s) {return !s.equals(defaultStringValue);}
	@Override public String getStringValue() {return stringValue;}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, this));}
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(stringValue);}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if(element instanceof JsonPrimitive primitive){
			String old = stringValue;
			stringValue = primitive.getAsString();
			return new UpdateTodo().valueChanged(!old.equals(stringValue));
		}
		else return setValueFailed(element);
	}
}
