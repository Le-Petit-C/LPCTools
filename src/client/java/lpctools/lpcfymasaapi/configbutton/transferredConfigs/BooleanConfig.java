package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BooleanConfig extends ConfigBoolean implements ILPC_MASAConfigWrapper<ConfigBoolean>, BooleanSupplier, BooleanConsumer {
    public BooleanConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, boolean defaultBoolean){
        this(defaultParent, nameKey, defaultBoolean, null);
    }
    public BooleanConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultBoolean);
        data = new Data(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public boolean getAsBoolean() {return getBooleanValue();}
    @Override public void accept(boolean b) {setBooleanValue(b);}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
