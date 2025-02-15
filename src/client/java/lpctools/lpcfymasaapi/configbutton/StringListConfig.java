package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigStringList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StringListConfig extends LPCConfig{
    public StringListConfig(LPCConfigList list, String name, ImmutableList<String> defaultValue){
        this.list = list;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public void setCallback(IValueChangeCallback<ConfigStringList> callback){
        this.callback = callback;
        if(instance != null)
            instance.setValueChangeCallback(callback);
    }

    public StringListConfig(LPCConfigList list, String name, ImmutableList<String> defaultValue, IValueChangeCallback<ConfigStringList> callback){
        this.list = list;
        this.name = name;
        this.defaultValue = defaultValue;
        setCallback(callback);
    }

    @Override
    public IConfigBase getConfig() {
        if(instance == null)
            instance = new StringListConfigInstance(this);
        return instance;
    }

    @NotNull
    public List<String> getStrings(){
        if(instance != null)
            return instance.getStrings();
        else return defaultValue;
    }

    @Nullable
    public IConfigStringList getStringConfig(){
        return instance;
    }

    @Override
    public boolean hasHotkey() {
        return false;
    }

    private StringListConfigInstance instance = null;
    private final LPCConfigList list;
    private final String name;
    private final ImmutableList<String> defaultValue;
    private IValueChangeCallback<ConfigStringList> callback;

    private static class StringListConfigInstance extends ConfigStringList{
        public StringListConfigInstance(StringListConfig parent) {
            super(parent.name, parent.defaultValue);
            apply(parent.list.getFullTranslationKey());
            setValueChangeCallback(parent.callback);
        }
    }

    @Override
    public LPCConfigPage getPage(){
        return list.getPage();
    }

    @Override
    public LPCConfigList getList(){
        return list;
    }
}
