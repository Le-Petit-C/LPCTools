package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.IBooleanConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniqueBooleanHotkeyConfig extends UniqueHotkeyConfig implements IBooleanConfig {
	protected boolean booleanValue;
	public final boolean defaultBoolean;
	public UniqueBooleanHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, defaultStorageString, callback);
		booleanValue = this.defaultBoolean = defaultBoolean;
		getKeybind().setCallback(((action, key) -> {toggleBooleanValue(); return true;}));
	}
	@Override public void resetToDefault() {
		booleanValue = defaultBoolean;
		super.resetToDefault();
		onValueChanged();
	}
	@Override public boolean isModified() {return super.isModified() || defaultBoolean != booleanValue;}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, this));
		super.getButtonOptions(res);
	}
	@Override public boolean getBooleanValue() {return booleanValue;}
	@Override public boolean getDefaultBooleanValue() {return defaultBoolean;}
	@Override public void setBooleanValue(boolean value) {
		if(value != booleanValue){
			booleanValue = value;
			onValueChanged();
		}
	}
	@Override public @NotNull JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add("hotkey", super.getAsJsonElement());
		object.addProperty(booleanJsonId, booleanValue);
		return object;
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		if(data instanceof JsonObject object){
			UpdateTodo todo = new UpdateTodo();
			if(object.get("hotkey") instanceof JsonElement element)
				todo.combine(super.setValueFromJsonElementEx(element));
			if(object.get(booleanJsonId) instanceof JsonPrimitive primitive){
				boolean old = booleanValue;
				booleanValue = primitive.getAsBoolean();
				todo.valueChanged(old != booleanValue);
			}
			return todo;
		}
		else return setValueFailed(data);
	}
}
