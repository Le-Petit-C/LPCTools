package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigBoolean;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniqueBooleanConfig extends LPCUniqueConfigBase implements IConfigBoolean {
    public boolean booleanValue;
    public final boolean defaultBoolean;
    public UniqueBooleanConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        this.defaultBoolean = defaultBoolean;
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, this));
    }
    @Override public @Nullable JsonPrimitive getAsJsonElement() {return new JsonPrimitive(booleanValue);}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        boolean old = booleanValue;
        if(element instanceof JsonPrimitive primitive)
            booleanValue = primitive.getAsBoolean();
        return new UpdateTodo().valueChanged(old != booleanValue);
    }
    @Override public boolean getBooleanValue() {return booleanValue;}
    @Override public boolean getDefaultBooleanValue() {return defaultBoolean;}
    @Override public void setBooleanValue(boolean b) {
        if(b != booleanValue){
            booleanValue = b;
            onValueChanged();
        }
    }
    @Override public boolean isModified() {return booleanValue != defaultBoolean;}
    @Override public void resetToDefault() {setBooleanValue(defaultBoolean);}
    @Override public String getDefaultStringValue() {return String.valueOf(defaultBoolean);}
    @Override public void setValueFromString(String s) {booleanValue = Boolean.parseBoolean(s);}
    @Override public boolean isModified(String s) {return Boolean.parseBoolean(s) != defaultBoolean;}
    @Override public String getStringValue() {return String.valueOf(booleanValue);}
}
