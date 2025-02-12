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

import java.util.ArrayList;

//配置列表
public class LPCConfigList {
    private final String translationKey;
    private final LPCConfigPage parent;
    private JsonObject configListJson;
    private boolean hasHotkeyConfig;
    public ArrayList<LPCConfig> uninitializedConfigs;
    public ArrayList<IConfigBase> configs;

    public LPCConfigPage getPage(){
        return parent;
    }

    public LPCConfigList(LPCConfigPage parent, String translationKey){
        this.parent = parent;
        this.translationKey = translationKey;
        if(LPCAPIInit.MASAInitialized) afterInit();
        else uninitializedConfigs = new ArrayList<>();
    }

    public void afterInit(){
        if(uninitializedConfigs == null) return;
        configs = new ArrayList<>();
        for(LPCConfig config : uninitializedConfigs){
            configs.add(config.getConfig());
            reloadConfigFromJson(configs.getLast());
        }
        uninitializedConfigs = null;
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

    public void addHotkeyConfig(String name, String defaultStorageString, IHotkeyCallback callBack){
        addConfig(new HotkeyConfig(this, name, defaultStorageString, callBack));
    }

    public void addBooleanHotkeyConfig(String name, Boolean defaultBoolean, String defaultStorageString){
        addConfig(new BooleanHotkeyConfig(this, name, defaultBoolean, defaultStorageString));
    }

    public void addBooleanHotkeyConfig(String name, Boolean defaultBoolean, String defaultStorageString, IValueChangeCallback<ConfigBoolean> callback){
        addConfig(new BooleanHotkeyConfig(this, name, defaultBoolean, defaultStorageString, callback));
    }

    public void addStringListConfig(String name, ImmutableList<String> defaultValue){
        addConfig(new StringListConfig(this, name, defaultValue));
    }

    public void addStringListConfig(String name, ImmutableList<String> defaultValue, IValueChangeCallback<ConfigStringList> callback){
        addConfig(new StringListConfig(this, name, defaultValue, callback));
    }

    public void addConfigOpenGuiConfig(String defaultStorageString){
        addConfig(new ConfigOpenGuiConfig(this, defaultStorageString));
    }

    public String getTitleFullTranslationKey(){
        return parent.getModReference().modId + ".configs." + translationKey + ".title";
    }

    public String getTranslationKey(){
        return translationKey;
    }

    public String getFullTranslationKey(){
        return getPage().getModReference().modId + ".configs." + getTranslationKey();
    }

    public String getTitleDisplayName(){
        return StringUtils.translate(getTitleFullTranslationKey());
    }

    public void resetListJson(JsonObject configPageJson){
        configListJson = JsonUtils.getNestedObject(configPageJson, getTranslationKey(), true);
        for (IConfigBase option : configs)
            reloadConfigFromJson(option);
    }

    private void reloadConfigFromJson(IConfigBase config){
        if(config == null) return;
        if(configListJson == null) return;
        String name = config.getName();
        if (!configListJson.has(name)) return;
        config.setValueFromJsonElement(configListJson.get(name));
    }

    public void reloadConfigJson(){
        for(IConfigBase config : configs)
            configListJson.add(config.getName(), config.getAsJsonElement());
    }

    public boolean hasHotkeyConfig() {
        return hasHotkeyConfig;
    }
}
