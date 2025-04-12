package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCHotkey;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BooleanHotkeyConfig extends LPCConfig<ConfigBooleanHotkeyed> implements ILPCHotkey, BooleanSupplier, BooleanConsumer {
    public final boolean defaultBoolean;
    public final String defaultStorageString;
    public BooleanHotkeyConfig(@NotNull ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        super(list, nameKey, true);
        this.defaultBoolean = defaultBoolean;
        this.defaultStorageString = defaultStorageString;
        list.getPage().getInputHandler().addHotkey(this);
    }
    public BooleanHotkeyConfig(@NotNull ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable IValueRefreshCallback callback){
        this(list, nameKey, defaultBoolean, defaultStorageString);
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
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
