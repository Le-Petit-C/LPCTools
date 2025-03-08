package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends LPCConfig<ConfigDouble>{
    public final double defaultDouble;
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble){
        super(list, name, false);
        this.defaultDouble = defaultDouble;
    }
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble, IValueRefreshCallback callback){
        this(list, name, defaultDouble);
        setCallback(callback);
    }
    public double getValue(){return getInstance() != null ? getInstance().getDoubleValue() : defaultDouble;}
    @Override @NotNull public ConfigDouble createInstance(){
        ConfigDouble config = new ConfigDouble(getNameKey(), defaultDouble, getCommentKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
