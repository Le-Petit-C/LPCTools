package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorConfig extends ConfigColor implements ILPC_MASAConfigWrapper<ConfigColor>, Supplier<Color4f>, Consumer<Color4f> {
    public ColorConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Color4f defaultColor){
        this(parent, nameKey, defaultColor, null);
    }
    public ColorConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultColor.toString());
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        Color4f lastValue = getColor();
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(!lastValue.equals(getColor()));
    }
    @Override public Color4f get() {return getColor();}
    @Override public void accept(Color4f color) {setIntegerValue(color.intValue);}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
