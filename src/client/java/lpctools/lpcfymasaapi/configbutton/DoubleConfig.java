package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleConfig extends LPCConfig<ConfigDouble> implements DoubleSupplier, DoubleConsumer {
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
    @Override @NotNull protected ConfigDouble createInstance(){
        ConfigDouble config = new ConfigDouble(nameKey, defaultDouble, minValue, maxValue);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
    @Override public void accept(double value) {
        getConfig().setDoubleValue(value);
    }
    @Override public double getAsDouble() {
        return getInstance() != null ? getInstance().getDoubleValue() : defaultDouble;
    }
}
