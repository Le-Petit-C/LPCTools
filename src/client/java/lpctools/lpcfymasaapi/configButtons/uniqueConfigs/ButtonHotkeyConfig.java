package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.hotkeys.*;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ButtonHotkeyConfig extends ButtonConfig implements IHotkey, AutoCloseable {
    public final @NotNull IKeybind keybind;
    public BooleanSupplier callback;
    public ButtonHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultKeyBindStorageString, @Nullable BooleanSupplier callback) {
        super(parent, nameKey);
        this.callback = callback;
        keybind = KeybindMulti.fromStorageString(defaultKeyBindStorageString == null ? "" : defaultKeyBindStorageString, KeybindSettings.DEFAULT);
        setListener(this::callCallback);
        keybind.setCallback(this::callCallback);
        getPage().getInputHandler().addHotkey(this);
    }
    public ButtonHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultKeyBindStorageString, @NotNull Runnable callback) {
        this(parent, nameKey, defaultKeyBindStorageString, () -> { callback.run(); return true; });
    }
    public ButtonHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultKeyBindStorageString) {
        this(parent, nameKey, defaultKeyBindStorageString, (BooleanSupplier) null);
    }
    @Override public @NotNull IKeybind getKeybind() {return keybind;}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        res.add(ILPCUniqueConfigBase.buttonKeybindPreset(1, this));
    }
    @Override public @Nullable JsonElement getAsJsonElement() { return keybind.getAsJsonElement(); }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        UpdateTodo todo = super.setValueFromJsonElementEx(data);
        keybind.setValueFromJsonElement(data);
        return todo.valueChanged();
    }
    private boolean callCallback(Object... ignored){if(callback != null) return callback.getAsBoolean(); else return false;}
    @Override public void close() { getPage().getInputHandler().removeHotkey(this); }
    @Override public boolean isDirty() { return keybind.isDirty() || super.isDirty(); }
}
