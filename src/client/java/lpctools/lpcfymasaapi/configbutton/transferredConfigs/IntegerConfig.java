package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.LPCConfigUtils;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.implementations.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.implementations.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class IntegerConfig extends ConfigInteger implements ILPC_MASAConfigWrapper<ConfigInteger>,IntSupplier, IntConsumer {
    public IntegerConfig(ILPCConfigList parent, String nameKey, int defaultInteger){
        this(parent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    public IntegerConfig(ILPCConfigList parent, String nameKey, int defaultInteger, ILPCValueChangeCallback callback){
        this(parent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, callback);
    }
    public IntegerConfig(ILPCConfigList parent, String nameKey, int defaultInteger, int minValue, int maxValue){
        this(parent, nameKey, defaultInteger, minValue, maxValue, null);
    }
    public IntegerConfig(ILPCConfigList parent, String nameKey, int defaultInteger, int minValue, int maxValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultInteger, minValue, maxValue);
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public int setMax(int value){return LPCConfigUtils.muteMaxValue(this, value);}
    public int setMin(int value){return LPCConfigUtils.muteMinValue(this, value);}

    @Override public void setValueFromJsonElement(JsonElement element) {
        int lastInt = getAsInt();
        super.setValueFromJsonElement(element);
        if(lastInt != getAsInt()) onValueChanged();
    }

    @Override public int getAsInt() {return getIntegerValue();}
    @Override public void accept(int value) {setIntegerValue(value);}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
