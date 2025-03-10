package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends LPCConfig<ConfigDouble>{
    public final double defaultDouble;
    public final double minValue, maxValue;
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble){
        this(list, name, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, null);
    }
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble, IValueRefreshCallback callback){
        this(list, name, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, callback);
    }
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble, double minValue, double maxValue){
        this(list, name, defaultDouble, minValue, maxValue, null);
    }
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble, double minValue, double maxValue, IValueRefreshCallback callback){
        super(list, name, false);
        this.defaultDouble = defaultDouble;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setCallback(callback);
    }
    public double getValue(){return getInstance() != null ? getInstance().getDoubleValue() : defaultDouble;}
    @Override @NotNull public ConfigDouble createInstance(){
        ConfigDouble config = new ConfigDouble(name, defaultDouble, minValue, maxValue);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
