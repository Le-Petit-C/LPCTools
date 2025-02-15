package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotkeyConfig extends LPCConfig<ConfigHotkey> {
    @Nullable public final String defaultStorageString;
    @NotNull public final IHotkeyCallback hotkeyCallback;
    public HotkeyConfig(@NotNull LPCConfigList list, @NotNull String name, @Nullable String defaultStorageString, @NotNull IHotkeyCallback hotkeyCallback){
        super(list, name, true);
        this.defaultStorageString = defaultStorageString;
        this.hotkeyCallback = hotkeyCallback;
    }

    @Override @NotNull protected ConfigHotkey createInstance(){
        ConfigHotkey config = new ConfigHotkey(name, defaultStorageString);
        config.apply(list.getFullTranslationKey());
        config.getKeybind().setCallback(hotkeyCallback);
        list.getPage().getInputHandler().addHotkey(config);
        return config;
    }
}
