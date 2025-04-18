package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigColor;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorConfig extends LPCConfig<ConfigColor> implements IntSupplier, IntConsumer {
    public final int defaultColor;
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, int defaultColor){
        this(defaultParent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, int defaultColor, @Nullable IValueRefreshCallback callback){
        super(defaultParent, nameKey, false, callback);
        this.defaultColor = defaultColor;
    }
    @Override public int getAsInt() {
        return getInstance() != null ? getInstance().getColor().intValue : defaultColor;
    }
    @Override public void accept(int color) {
        getConfig().setIntegerValue(color);
    }
    @Override @NotNull protected ConfigColor createInstance(){
        ConfigColor config = new ConfigColor(getNameKey(), String.format("0x%x", defaultColor));
        config.setValueChangeCallback(new LPCConfig.LPCConfigCallback<>(this));
        return config;
    }
}
