package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

public class IntegerConfig extends LPCConfig<ConfigInteger>{
    public final int defaultInteger;
    public final int minValue, maxValue;
    public IntegerConfig(LPCConfigList list, String name, int defaultInteger){
        this(list, name, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    public IntegerConfig(LPCConfigList list, String name, int defaultInteger, IValueRefreshCallback callback){
        this(list, name, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, callback);
    }
    public IntegerConfig(LPCConfigList list, String name, int defaultInteger, int minValue, int maxValue){
        this(list, name, defaultInteger, minValue, maxValue, null);
    }
    public IntegerConfig(LPCConfigList list, String name, int defaultInteger, int minValue, int maxValue, IValueRefreshCallback callback){
        super(list, name, false);
        this.defaultInteger = defaultInteger;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setCallback(callback);
    }
    public int getValue(){return getInstance() != null ? getInstance().getIntegerValue() : defaultInteger;}
    @Override @NotNull public ConfigInteger createInstance(){
        ConfigInteger config = new ConfigInteger(name, defaultInteger, minValue, maxValue);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
