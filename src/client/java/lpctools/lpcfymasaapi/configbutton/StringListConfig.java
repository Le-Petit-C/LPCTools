package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListConfig extends LPCConfig<ConfigStringList>{
    public StringListConfig(LPCConfigList list, String nameKey, @NotNull ImmutableList<String> defaultValue){
        super(list, nameKey, false);
        this.defaultValue = defaultValue;
    }
    public StringListConfig(LPCConfigList list, String nameKey, ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        this(list, nameKey, defaultValue);
        refreshCallback = callback;
    }
    @NotNull public List<String> getStrings(){return getInstance() != null ? getInstance().getStrings() : defaultValue;}

    @Override protected @NotNull ConfigStringList createInstance() {
        ConfigStringList config = new ConfigStringList(getTranslationKey(), defaultValue);
        config.apply(getList().getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }

    private final ImmutableList<String> defaultValue;
}
