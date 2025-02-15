package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends LPCConfig<ConfigBoolean> {
    public final boolean defaultBoolean;
    public BooleanConfig(LPCConfigList list, String name, boolean defaultBoolean){
        super(list, name, false);
        this.defaultBoolean = defaultBoolean;
    }
    public BooleanConfig(LPCConfigList list, String name, boolean defaultBoolean, IValueRefreshCallback callback){
        this(list, name, defaultBoolean);
        setCallback(callback);
    }
    public boolean getValue(){return getInstance() != null ?  getInstance().getBooleanValue() : defaultBoolean ;}
    @Override @NotNull protected ConfigBoolean createInstance(){
        ConfigBoolean config = new ConfigBoolean(name, defaultBoolean);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
