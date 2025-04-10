package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.options.ConfigString;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringConfig extends LPCConfig<ConfigString> implements Supplier<String>, Consumer<String> {
    private final @NotNull String defaultString;
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable String defaultString, @Nullable IValueRefreshCallback callback) {
        super(defaultParent, nameKey, false, callback);
        this.defaultString = defaultString == null ? "" : defaultString;
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable String defaultString) {
        this(defaultParent, nameKey, defaultString, null);
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable IValueRefreshCallback callback) {
        this(defaultParent, nameKey, null, callback);
    }
    public StringConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey) {
        this(defaultParent, nameKey, null, null);
    }

    @Override public void accept(String s) {
        getConfig().setValueFromString(s);
    }

    @Override @NotNull public String get() {
        ConfigString instance = getInstance();
        if(instance == null) return defaultString;
        else return instance.getStringValue();
    }

    @Override protected @NotNull ConfigString createInstance() {
        ConfigString config = new ConfigString(getNameKey(), defaultString);
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
