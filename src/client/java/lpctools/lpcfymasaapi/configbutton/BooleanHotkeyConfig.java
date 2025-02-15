package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class BooleanHotkeyConfig extends LPCConfig<ConfigBooleanHotkeyed> {
    public final boolean defaultBoolean;
    public final String defaultStorageString;
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString){
        super(list, name, true);
        this.defaultBoolean = defaultBoolean;
        this.defaultStorageString = defaultStorageString;
    }
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString, IValueRefreshCallback callback){
        this(list, name, defaultBoolean, defaultStorageString);
        setCallback(callback);
    }

    @Override @NotNull protected ConfigBooleanHotkeyed createInstance() {
        ConfigBooleanHotkeyed config = new ConfigBooleanHotkeyed(getName(), defaultBoolean, defaultStorageString);
        config.apply(list.getFullTranslationKey());
        list.getPage().getInputHandler().addHotkey(config);
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
