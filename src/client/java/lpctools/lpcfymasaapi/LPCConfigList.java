package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

//配置列表
public class LPCConfigList {
    public LPCConfigPage getPage(){return parent;}
    public LPCConfigList(LPCConfigPage parent, String translationKey){
        this.parent = parent;
        this.translationKey = translationKey;
    }
    //加入的配置无法删除，但是你可以将LPCConfig.enabled置为false让它不显示
    public void addConfig(LPCConfig config){
        configs.add(config);
        if(LPCAPIInit.MASAInitialized)
            reloadConfigFromJson(config);
        if(config.hasHotkey())
            hasHotkeyConfig = true;
    }
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
    public DoubleConfig addDoubleConfig(String name, double defaultDouble){
        DoubleConfig config = new DoubleConfig(this, name, defaultDouble);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String name, double defaultDouble, IValueChangeCallback<ConfigDouble> callback){
        DoubleConfig config = new DoubleConfig(this, name, defaultDouble, callback);
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
        for (LPCConfig option : configs)
            reloadConfigFromJson(option);
    }
    public void reloadConfigJson(){
        for(LPCConfig config : configs)
            configListJson.add(config.getConfig().getName(), config.getConfig().getAsJsonElement());
    }
    public boolean hasHotkeyConfig() {return hasHotkeyConfig;}
    public void setCallback(@Nullable IConfigListCallback callback){this.callback = callback;}
    @Nullable public IConfigListCallback getCallback(){return callback;}

    @NotNull ArrayList<LPCConfig> configs = new ArrayList<>();

    private final String translationKey;
    private final LPCConfigPage parent;
    private JsonObject configListJson;
    private boolean hasHotkeyConfig;
    @Nullable private IConfigListCallback callback = null;
    private void reloadConfigFromJson(LPCConfig config){
        if(config == null) return;
        if(configListJson == null) return;
        String name = config.getName();
        if (!configListJson.has(name)) return;
        config.setValueFromJsonElement(configListJson.get(name));
    }
}
