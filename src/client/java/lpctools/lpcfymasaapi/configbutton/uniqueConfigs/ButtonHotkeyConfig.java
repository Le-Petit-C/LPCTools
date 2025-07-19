package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.hotkeys.*;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ButtonHotkeyConfig extends ButtonConfig implements IHotkey, AutoCloseable {
    public final @NotNull IKeybind keybind;
    public Runnable callback;
    public ButtonHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultKeyBindStorageString, @Nullable Runnable callback) {
        super(parent, nameKey);
        this.callback = callback;
        keybind = KeybindMulti.fromStorageString(defaultKeyBindStorageString == null ? "" : defaultKeyBindStorageString, KeybindSettings.DEFAULT);
        setListener(this::callCallback);
        keybind.setCallback(this::callCallback);
        getPage().getInputHandler().addHotkey(this);
    }
    @Override public @NotNull IKeybind getKeybind() {return keybind;}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        res.add(ILPCUniqueConfigBase.buttonKeybindPreset(1, this));
    }
    @Override public @Nullable JsonElement getAsJsonElement() {
        return keybind.getAsJsonElement();
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
        keybind.setValueFromJsonElement(data);
    }
    private boolean callCallback(Object... ignored){if(callback != null) callback.run(); return true;}
    @Override public void close() {
        getPage().getInputHandler().removeHotkey(this);
    }
}
