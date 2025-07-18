package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class UniqueHotkeyConfig extends LPCUniqueConfigBase implements IHotkey, AutoCloseable {
    public final @NotNull String defaultStorageString;
    private final IKeybind keybind;
    public UniqueHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        this.defaultStorageString = defaultStorageString == null ? "" : defaultStorageString;
        keybind = KeybindMulti.fromStorageString(defaultStorageString, KeybindSettings.DEFAULT);
        getPage().getInputHandler().addHotkey(this);
    }
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {res.add(ILPCUniqueConfigBase.buttonKeybindPreset(1, this));}
    @Override public @Nullable JsonElement getAsJsonElement() {return keybind.getAsJsonElement();}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        String lastStorageString = keybind.getStringValue();
        keybind.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(lastStorageString.equals(keybind.getStringValue()));
    }
    @Override public IKeybind getKeybind() {return keybind;}
    @Override public void close() {getPage().getInputHandler().removeHotkey(this);}
}
