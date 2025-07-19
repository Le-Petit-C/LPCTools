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

public class BooleanThirdListConfig extends ThirdListConfig implements IBooleanConfig {
    protected boolean booleanValue;
    private final boolean defaultBoolean;
    public BooleanThirdListConfig(ILPCConfigReadable parent, String nameKey, boolean defaultBoolean, ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        booleanValue = this.defaultBoolean = defaultBoolean;
    }
    @Override public boolean getBooleanValue() {return booleanValue;}
    @Override public boolean getDefaultBooleanValue() {return defaultBoolean;}
    @Override public void setBooleanValue(boolean value) {
        if(value != booleanValue){
            booleanValue = value;
            onValueChanged();
        }
    }
    @Override public boolean isModified() {return booleanValue != defaultBoolean;}
    @Override public void setExpanded(boolean expanded) {
        if(expanded != isExpanded()){
            this.expanded = expanded;
            getPage().updateIfCurrent();
        }
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, this));
    }
    @Override public void resetToDefault() {setBooleanValue(getDefaultBooleanValue());}
    @Override public String getDefaultStringValue() {return String.valueOf(getDefaultBooleanValue());}
    @Override public void setValueFromString(String value) {setBooleanValue(Boolean.parseBoolean(value));}
    @Override public boolean isModified(String newValue) {return Boolean.parseBoolean(newValue) != defaultBoolean;}
    @Override public String getStringValue() {return String.valueOf(getBooleanValue());}
    @Override public @NotNull JsonObject getAsJsonElement() {
        JsonObject res = super.getAsJsonElement();
        res.addProperty(booleanJsonId, booleanValue);
        return res;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        UpdateTodo todo = super.setValueFromJsonElementEx(element);
        if(element instanceof JsonObject object && object.get(booleanJsonId) instanceof JsonPrimitive primitive){
            todo.valueChanged(booleanValue != primitive.getAsBoolean());
            booleanValue = primitive.getAsBoolean();
        }
        return todo;
    }
}
