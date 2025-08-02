package lpctools.lpcfymasaapi.configButtons.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigString;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringConfig extends ConfigString implements ILPC_MASAConfigWrapper<ConfigString>, Supplier<String>, Consumer<String> {
    public StringConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback) {
        super(nameKey, defaultString);
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public StringConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable String defaultString) {
        this(parent, nameKey, defaultString, null);
    }
    public StringConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        this(parent, nameKey, null, callback);
    }
    public StringConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey) {
        this(parent, nameKey, null, null);
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        String lastString = get();
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(!lastString.equals(get()));
    }
    @Override public void accept(String s) {setValueFromString(s);}
    @Override @NotNull public String get() {return getStringValue();}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
