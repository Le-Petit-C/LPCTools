package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//一个有三个按钮：展开，boolean，hotkey的配置
public class BooleanHotkeyThirdListConfig extends BooleanThirdListConfig implements IHotkey, AutoCloseable {
    private final @NotNull IKeybind keybind;
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigReadable parent, String nameKey, boolean defaultBooleanValue, @Nullable String defaultHotkey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, defaultBooleanValue, callback);
        keybind = KeybindMulti.fromStorageString(defaultHotkey == null ? "" : defaultHotkey, KeybindSettings.DEFAULT);
        keybind.setCallback((action, bind)->{toggleBooleanValue(); return true;});
        parent.getPage().getInputHandler().addHotkey(this);
    }
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey){
        this(parent, nameKey, false, null, null);
    }
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this(parent, nameKey, false, null, callback);
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        res.add(ILPCUniqueConfigBase.buttonKeybindPreset(3, this));
    }
    @Override public boolean isModified() {
        return super.isModified() && keybind.isModified();
    }
    @Override public void resetToDefault() {
        if(isModified()){
            keybind.resetToDefault();
            booleanValue = getDefaultBooleanValue();
            getPage().updateIfCurrent();
            onValueChanged();
        }
    }
    @Override public @NotNull IKeybind getKeybind() {return keybind;}
    
    public final String hotkeyJsonId = "hotkey";
    @Override public @NotNull JsonObject getAsJsonElement() {
        JsonObject object = super.getAsJsonElement();
        object.add(hotkeyJsonId, keybind.getAsJsonElement());
        return object;
    }
    
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        UpdateTodo todo = super.setValueFromJsonElementEx(data);
        if(data instanceof JsonObject object && object.get(hotkeyJsonId) instanceof JsonElement element){
            String str = keybind.getStringValue();
            keybind.setValueFromJsonElement(element);
            todo.valueChanged(!keybind.getStringValue().equals(str));
        }
        return todo;
    }
    @Override public void close() {
        getPage().getInputHandler().removeHotkey(this);
    }
}
