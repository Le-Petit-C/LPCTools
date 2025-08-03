package lpctools.lpcfymasaapi.configButtons.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.LPCConfigUtils;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class IntegerConfig extends ConfigInteger implements ILPC_MASAConfigWrapper<ConfigInteger>,IntSupplier, IntConsumer {
    public IntegerConfig(ILPCConfigReadable parent, String nameKey, int defaultInteger){
        this(parent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    public IntegerConfig(ILPCConfigReadable parent, String nameKey, int defaultInteger, ILPCValueChangeCallback callback){
        this(parent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, callback);
    }
    public IntegerConfig(ILPCConfigReadable parent, String nameKey, int defaultInteger, int minValue, int maxValue){
        this(parent, nameKey, defaultInteger, minValue, maxValue, null);
    }
    public IntegerConfig(ILPCConfigReadable parent, String nameKey, int defaultInteger, int minValue, int maxValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultInteger, minValue, maxValue, "");
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public int setMax(int value){return LPCConfigUtils.muteMaxValue(this, value);}
    public int setMin(int value){return LPCConfigUtils.muteMinValue(this, value);}
    public IntegerConfig useSlider(boolean b){
        if(shouldUseSlider() != b) toggleUseSlider();
        return this;
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        int lastInt = getAsInt();
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(lastInt != getAsInt());
    }

    @Override public int getAsInt() {return getIntegerValue();}
    @Override public void accept(int value) {setIntegerValue(value);}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
