package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntegerConfig extends ConfigInteger implements ILPC_MASAConfigWrapper<ConfigInteger>,IntSupplier, IntConsumer {
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger){
        this(defaultParent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger, ILPCValueChangeCallback callback){
        this(defaultParent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, callback);
    }
    public IntegerConfig(ILPCConfigList defaultParent, String nameKey, int defaultInteger, int minValue, int maxValue){
        this(defaultParent, nameKey, defaultInteger, minValue, maxValue, null);
    }
    public IntegerConfig(ILPCConfigList parent, String nameKey, int defaultInteger, int minValue, int maxValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultInteger, minValue, maxValue);
        data = new Data(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public int getAsInt() {return getIntegerValue();}
    @Override public void accept(int value) {setIntegerValue(value);}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
