package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleConfig extends LPCConfig<ConfigDouble> implements DoubleSupplier, DoubleConsumer {
    public final double defaultDouble;
    public final double minValue, maxValue;
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble){
        this(defaultParent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, null);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, IValueRefreshCallback callback){
        this(defaultParent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, callback);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, double minValue, double maxValue){
        this(defaultParent, nameKey, defaultDouble, minValue, maxValue, null);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, double minValue, double maxValue, IValueRefreshCallback callback){
        super(defaultParent, nameKey, false);
        this.defaultDouble = defaultDouble;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setCallback(callback);
    }
    @Override @NotNull protected ConfigDouble createInstance(){
        ConfigDouble config = new ConfigDouble(getNameKey(), defaultDouble, minValue, maxValue);
        config.apply(getDefaultParent().getFullTranslationKey());
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
