package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigString;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringConfig extends LPCConfig<ConfigString> implements Supplier<String>, Consumer<String> {
    private final @NotNull String defaultString;
    public StringConfig(@NotNull LPCConfigList list, @NotNull String nameKey, @Nullable String defaultString, @Nullable IValueRefreshCallback callback) {
        super(list, nameKey, false, callback);
        this.defaultString = defaultString == null ? "" : defaultString;
    }
    public StringConfig(@NotNull LPCConfigList list, @NotNull String nameKey, @Nullable String defaultString) {
        this(list, nameKey, defaultString, null);
    }
    public StringConfig(@NotNull LPCConfigList list, @NotNull String nameKey, @Nullable IValueRefreshCallback callback) {
        this(list, nameKey, null, callback);
    }
    public StringConfig(@NotNull LPCConfigList list, @NotNull String nameKey) {
        this(list, nameKey, null, null);
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
        config.apply(getList().getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
