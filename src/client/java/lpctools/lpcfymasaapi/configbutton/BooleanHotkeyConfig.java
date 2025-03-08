package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class BooleanHotkeyConfig extends LPCConfig<ConfigBooleanHotkeyed> implements ILPCHotkey{
    public final boolean defaultBoolean;
    public final String defaultStorageString;
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString){
        super(list, name, true);
        this.defaultBoolean = defaultBoolean;
        this.defaultStorageString = defaultStorageString;
        list.getPage().getInputHandler().addHotkey(this);
    }
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString, IValueRefreshCallback callback){
        this(list, name, defaultBoolean, defaultStorageString);
        setCallback(callback);
    }
    @Override public IHotkey LPCGetHotkey() {return getConfig();}

    @Override @NotNull protected ConfigBooleanHotkeyed createInstance() {
        ConfigBooleanHotkeyed config = new ConfigBooleanHotkeyed(getNameKey(), defaultBoolean, defaultStorageString, getCommentKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
