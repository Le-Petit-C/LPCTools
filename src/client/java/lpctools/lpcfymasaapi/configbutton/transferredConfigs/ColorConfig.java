package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorConfig extends LPCConfig<ConfigColor> implements Supplier<Color4f>, Consumer<Color4f> {
    public final Color4f defaultColor;
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, Color4f defaultColor){
        this(defaultParent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, Color4f defaultColor, @Nullable IValueRefreshCallback callback){
        super(defaultParent, nameKey, false, callback);
        this.defaultColor = defaultColor;
    }
    @Override public Color4f get() {
        return getInstance() != null ? getInstance().getColor() : defaultColor;
    }
    @Override public void accept(Color4f color) {
        getConfig().setIntegerValue(color.getIntValue());
    }
    @Override @NotNull protected ConfigColor createInstance(){
        ConfigColor config = new ConfigColor(getNameKey(), defaultColor);
        config.setValueChangeCallback(new LPCConfig.LPCConfigCallback<>(this));
        return config;
    }
}
