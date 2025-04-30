package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorConfig extends ConfigColor implements ILPC_MASAConfigWrapper<ConfigInteger>, IntSupplier, IntConsumer {
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, int defaultColor){
        this(defaultParent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, int defaultColor, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, String.format("0x%x", defaultColor));
        data = new Data(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public int getAsInt() {return getColor().intValue;}
    @Override public void accept(int color) {setIntegerValue(color);}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
