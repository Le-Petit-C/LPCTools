package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListConfig extends LPCConfig<ConfigStringList>{
    public StringListConfig(LPCConfigList list, String name, @NotNull ImmutableList<String> defaultValue){
        super(list, name, false);
        this.defaultValue = defaultValue;
    }

    public StringListConfig(LPCConfigList list, String name, ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        this(list, name, defaultValue);
        refreshCallback = callback;
    }

    @NotNull public List<String> getStrings(){return getInstance() != null ? getInstance().getStrings() : defaultValue;}

    private final ImmutableList<String> defaultValue;

    @Override
    protected @NotNull ConfigStringList createInstance() {
        ConfigStringList config = new ConfigStringList(name, defaultValue);
        config.apply(list.getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
