package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorConfig extends ConfigColor implements ILPC_MASAConfigWrapper<ConfigColor>, Supplier<Color4f>, Consumer<Color4f> {
    public ColorConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, Color4f defaultColor){
        this(defaultParent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultColor);
        data = new Data(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public Color4f get() {return getColor();}
    @Override public void accept(Color4f color) {setIntegerValue(color.getIntValue());}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
