package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotkeyConfig extends LPCConfig<ConfigHotkey> implements ILPCHotkey{
    @Nullable public final String defaultStorageString;
    @NotNull public final IHotkeyCallback hotkeyCallback;
    public HotkeyConfig(@NotNull LPCConfigList list, @NotNull String name, @Nullable String defaultStorageString, @NotNull IHotkeyCallback hotkeyCallback){
        super(list, name, true);
        this.defaultStorageString = defaultStorageString;
        this.hotkeyCallback = hotkeyCallback;
        list.getPage().getInputHandler().addHotkey(this);
    }
    @Override public IHotkey LPCGetHotkey() {return getConfig();}

    @Override @NotNull protected ConfigHotkey createInstance(){
        ConfigHotkey config = new ConfigHotkey(getNameKey(), defaultStorageString, getCommentKey());
        config.getKeybind().setCallback(hotkeyCallback);
        return config;
    }
}
