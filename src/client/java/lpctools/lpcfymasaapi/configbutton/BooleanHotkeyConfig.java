package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class BooleanHotkeyConfig extends LPCConfig<ConfigBooleanHotkeyed> implements ILPCHotkey, BooleanSupplier, BooleanConsumer {
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
    @Override public boolean getAsBoolean() {
        ConfigBooleanHotkeyed config = getInstance();
        if(config != null) return config.getBooleanValue();
        else return defaultBoolean;
    }
    @Override public void accept(boolean b) {
        getConfig().setBooleanValue(b);
    }

    @Override @NotNull protected ConfigBooleanHotkeyed createInstance() {
        ConfigBooleanHotkeyed config = new ConfigBooleanHotkeyed(getNameKey(), defaultBoolean, defaultStorageString);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
