package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.implementations.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.implementations.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorConfig extends ConfigColor implements ILPC_MASAConfigWrapper<ConfigColor>, Supplier<Color4f>, Consumer<Color4f> {
    public ColorConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, Color4f defaultColor){
        this(parent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultColor);
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }

    @Override public void setValueFromJsonElement(JsonElement element) {
        Color4f lastValue = getColor();
        super.setValueFromJsonElement(element);
        if(!lastValue.equals(getColor())) onValueChanged();
    }
    @Override public Color4f get() {return getColor();}
    @Override public void accept(Color4f color) {setIntegerValue(color.getIntValue());}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
