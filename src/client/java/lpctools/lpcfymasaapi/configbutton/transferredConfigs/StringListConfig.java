package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StringListConfig extends LPCConfig<ConfigStringList> {
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue){
        super(defaultParent, nameKey, false);
        this.defaultValue = defaultValue;
    }
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        this(defaultParent, nameKey, defaultValue);
        refreshCallback = callback;
    }
    @NotNull public List<String> getStrings(){
        ConfigStringList instance = getInstance();
        if(instance != null) return instance.getStrings();
        else if(defaultValue != null) return defaultValue;
        else return List.of();
    }

    @Override protected @NotNull ConfigStringList createInstance() {
        ConfigStringList config = new ConfigStringList(getNameKey(), defaultValue);
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }

    private final @Nullable ImmutableList<String> defaultValue;
}
