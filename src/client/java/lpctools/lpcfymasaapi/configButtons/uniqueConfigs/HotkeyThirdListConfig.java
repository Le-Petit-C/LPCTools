package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.hotkeys.*;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotkeyThirdListConfig extends ThirdListConfig implements IHotkey, AutoCloseable {
    private final @NotNull IKeybind keybind;
    public HotkeyThirdListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable String defaultHotkey, @Nullable ILPCValueChangeCallback callback, @Nullable IHotkeyCallback hotkeyCallback) {
        super(parent, nameKey, callback);
        keybind = KeybindMulti.fromStorageString(defaultHotkey == null ? "" : defaultHotkey, KeybindSettings.DEFAULT);
        keybind.setCallback(hotkeyCallback);
        parent.getPage().getInputHandler().addHotkey(this);
    }
    public HotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey){
        this(parent, nameKey, null, null, null);
    }
    public HotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this(parent, nameKey, null, callback, null);
    }
    public HotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable IHotkeyCallback hotkeyCallback){
        this(parent, nameKey, null, null, hotkeyCallback);
    }
    public HotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback, @Nullable IHotkeyCallback hotkeyCallback){
        this(parent, nameKey, null, callback, hotkeyCallback);
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        res.add(ILPCUniqueConfigBase.buttonKeybindPreset(3, this));
    }
    @Override public boolean isModified() { return keybind.isModified(); }
    @Override public void resetToDefault() {
        if(isModified()){
            keybind.resetToDefault();
            getPage().markNeedUpdate();
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
