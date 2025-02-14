package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

//配置列表
public class LPCConfigList {
    public LPCConfigPage getPage(){return parent;}
    public LPCConfigList(LPCConfigPage parent, String translationKey){
        this.parent = parent;
        this.translationKey = translationKey;
        if(LPCAPIInit.MASAInitialized) afterInit();
        else uninitializedConfigs = new ArrayList<>();
    }
    public void addConfig(LPCConfig config){
        if(uninitializedConfigs != null) uninitializedConfigs.add(config);
        if(configs != null){
            configs.add(config.getConfig());
            reloadConfigFromJson(configs.getLast());
        }
        if(config.hasHotkey())
            hasHotkeyConfig = true;
    }
    //TODO:removeConfig
    public BooleanConfig addBooleanConfig(String name, boolean defaultBoolean){
        BooleanConfig config = new BooleanConfig(this, name, defaultBoolean);
        addConfig(config);
        return config;
    }
    public BooleanConfig addBooleanConfig(String name, boolean defaultBoolean, IValueChangeCallback<ConfigBoolean> callback){
        BooleanConfig config = new BooleanConfig(this, name, defaultBoolean, callback);
        addConfig(config);
        return config;
    }
    public HotkeyConfig addHotkeyConfig(String name, String defaultStorageString, IHotkeyCallback callBack){
        HotkeyConfig config = new HotkeyConfig(this, name, defaultStorageString, callBack);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String name, boolean defaultBoolean, String defaultStorageString){
        BooleanHotkeyConfig config = new BooleanHotkeyConfig(this, name, defaultBoolean, defaultStorageString);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String name, boolean defaultBoolean, String defaultStorageString, IValueChangeCallback<ConfigBoolean> callback){
        BooleanHotkeyConfig config = new BooleanHotkeyConfig(this, name, defaultBoolean, defaultStorageString, callback);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String name, ImmutableList<String> defaultValue){
        StringListConfig config = new StringListConfig(this, name, defaultValue);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String name, ImmutableList<String> defaultValue, IValueChangeCallback<ConfigStringList> callback){
        StringListConfig config = new StringListConfig(this, name, defaultValue, callback);
        addConfig(config);
        return config;
    }
    public ConfigOpenGuiConfig addConfigOpenGuiConfig(String defaultStorageString){
        ConfigOpenGuiConfig config = new ConfigOpenGuiConfig(this, defaultStorageString);
        addConfig(config);
        return config;
    }
    public String getTitleFullTranslationKey(){return parent.getModReference().modId + ".configs." + translationKey + ".title";}
    public String getTranslationKey(){return translationKey;}
    public String getFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getTranslationKey();}
    public String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    public void resetListJson(JsonObject configPageJson){
        configListJson = JsonUtils.getNestedObject(configPageJson, getTranslationKey(), true);
        for (IConfigBase option : configs)
            reloadConfigFromJson(option);
    }
    public void reloadConfigJson(){
        for(IConfigBase config : configs)
            configListJson.add(config.getName(), config.getAsJsonElement());
    }
    public boolean hasHotkeyConfig() {return hasHotkeyConfig;}
    public void setCallback(@Nullable IConfigListCallback callback){this.callback = callback;}
    @Nullable public IConfigListCallback getCallback(){return callback;}

    ArrayList<IConfigBase> configs;
    void afterInit(){
        if(uninitializedConfigs == null) return;
        configs = new ArrayList<>();
        for(LPCConfig config : uninitializedConfigs){
            configs.add(config.getConfig());
            reloadConfigFromJson(configs.getLast());
        }
        uninitializedConfigs = null;
    }

    private ArrayList<LPCConfig> uninitializedConfigs;
    private final String translationKey;
    private final LPCConfigPage parent;
    private JsonObject configListJson;
    private boolean hasHotkeyConfig;
    @Nullable private IConfigListCallback callback = null;
    private void reloadConfigFromJson(IConfigBase config){
        if(config == null) return;
        if(configListJson == null) return;
        String name = config.getName();
        if (!configListJson.has(name)) return;
        config.setValueFromJsonElement(configListJson.get(name));
    }
}
