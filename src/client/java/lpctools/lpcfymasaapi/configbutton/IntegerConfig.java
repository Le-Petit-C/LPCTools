package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntegerConfig extends LPCConfig<ConfigInteger> implements IntSupplier, IntConsumer {
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
    @Override @NotNull protected ConfigInteger createInstance(){
        ConfigInteger config = new ConfigInteger(nameKey, defaultInteger, minValue, maxValue);
        config.apply(list.getFullTranslationKey());
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
