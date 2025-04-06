package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StringListConfig extends LPCConfig<ConfigStringList>{
    public StringListConfig(LPCConfigList list, String nameKey, @Nullable ImmutableList<String> defaultValue){
        super(list, nameKey, false);
        this.defaultValue = defaultValue;
    }
    public StringListConfig(LPCConfigList list, String nameKey, @Nullable ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        this(list, nameKey, defaultValue);
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
        config.apply(getList().getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }

    private final @Nullable ImmutableList<String> defaultValue;
}
