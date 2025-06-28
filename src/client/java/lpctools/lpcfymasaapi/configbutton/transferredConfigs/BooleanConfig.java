package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BooleanConfig extends ConfigBoolean implements ILPC_MASAConfigWrapper<ConfigBoolean>, BooleanSupplier, BooleanConsumer {
    public BooleanConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, boolean defaultBoolean){
        this(parent, nameKey, defaultBoolean, null);
    }
    public BooleanConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultBoolean, "");
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public boolean getAsBoolean() {return getBooleanValue();}
    @Override public void accept(boolean b) {setBooleanValue(b);}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        boolean lastValue = getBooleanValue();
        super.setValueFromJsonElement(element);
        if(lastValue != getBooleanValue()) onValueChanged();
    }
}
