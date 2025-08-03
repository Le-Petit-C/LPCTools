package lpctools.lpcfymasaapi.configButtons.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import lpctools.lpcfymasaapi.LPCConfigUtils;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class DoubleConfig extends ConfigDouble implements ILPC_MASAConfigWrapper<ConfigDouble>, DoubleSupplier, DoubleConsumer {
    public DoubleConfig(ILPCConfigReadable parent, String nameKey, double defaultDouble){
        this(parent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, null);
    }
    public DoubleConfig(ILPCConfigReadable parent, String nameKey, double defaultDouble, ILPCValueChangeCallback callback){
        this(parent, nameKey, defaultDouble, Double.MIN_VALUE, Double.MAX_VALUE, callback);
    }
    public DoubleConfig(ILPCConfigReadable parent, String nameKey, double defaultDouble, double minValue, double maxValue){
        this(parent, nameKey, defaultDouble, minValue, maxValue, null);
    }
    public DoubleConfig(ILPCConfigReadable parent, String nameKey, double defaultDouble, double minValue, double maxValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultDouble, minValue, maxValue, "");
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public double setMax(double value){return LPCConfigUtils.muteMaxValue(this, value);}
    public double setMin(double value){return LPCConfigUtils.muteMinValue(this, value);}
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        double lastValue = getAsDouble();
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(lastValue != getAsDouble());
    }
    @Override public void accept(double value) {setDoubleValue(value);}
    @Override public double getAsDouble() {return getDoubleValue();}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
