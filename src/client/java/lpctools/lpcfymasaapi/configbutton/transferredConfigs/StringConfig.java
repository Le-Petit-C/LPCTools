package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigString;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringConfig extends ConfigString implements ILPC_MASAConfigWrapper<ConfigString>, Supplier<String>, Consumer<String> {
    public StringConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback) {
        super(nameKey, defaultString);
        data = new Data(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable String defaultString) {
        this(defaultParent, nameKey, defaultString, null);
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        this(defaultParent, nameKey, null, callback);
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey) {
        this(defaultParent, nameKey, null, null);
    }

    @Override public void accept(String s) {setValueFromString(s);}
    @Override @NotNull public String get() {return getStringValue();}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
