package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleConfig extends ConfigDouble implements ILPC_MASAConfigWrapper<ConfigDouble>, DoubleSupplier, DoubleConsumer {
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble){
        this(defaultParent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, null);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, ILPCValueChangeCallback callback){
        this(defaultParent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, callback);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, double minValue, double maxValue){
        this(defaultParent, nameKey, defaultDouble, minValue, maxValue, null);
    }
    public DoubleConfig(ILPCConfigList defaultParent, String nameKey, double defaultDouble, double minValue, double maxValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultDouble, minValue, maxValue);
        data = new Data(defaultParent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public void accept(double value) {setDoubleValue(value);}
    @Override public double getAsDouble() {return getDoubleValue();}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
