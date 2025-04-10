package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntegerConfig extends LPCConfig<ConfigInteger> implements IntSupplier, IntConsumer {
    public final int defaultInteger;
    public final int minValue, maxValue;
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger){
        this(defaultParent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger, IValueRefreshCallback callback){
        this(defaultParent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, callback);
    }
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger, int minValue, int maxValue){
        this(defaultParent, nameKey, defaultInteger, minValue, maxValue, null);
    }
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger, int minValue, int maxValue, IValueRefreshCallback callback){
        super(defaultParent, nameKey, false);
        this.defaultInteger = defaultInteger;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setCallback(callback);
    }
    @Override @NotNull protected ConfigInteger createInstance(){
        ConfigInteger config = new ConfigInteger(getNameKey(), defaultInteger, minValue, maxValue);
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
    @Override public int getAsInt() {
        return getInstance() != null ? getInstance().getIntegerValue() : defaultInteger;
    }
    //accept不应在初始化时调用
    @Override public void accept(int value) {
        getConfig().setIntegerValue(value);
    }

}
