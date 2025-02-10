package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.ConfigOpenGuiConfig;
import lpctools.lpcfymasaapi.configbutton.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;

import java.util.ArrayList;

//配置列表
public class LPCConfigList {
    private final String translationKey;
    private final LPCConfigPage parent;
    public ArrayList<LPCConfig> uninitializedConfigs;
    public ArrayList<IConfigBase> configs;

    public LPCConfigList(LPCConfigPage parent, String translationKey){
        this.parent = parent;
        this.translationKey = translationKey;
        if(LPCAPIInit.MASAInitialized) afterInit();
        else uninitializedConfigs = new ArrayList<>();
    }

    public void afterInit(){
        if(uninitializedConfigs == null) return;
        configs = new ArrayList<>();
        for(LPCConfig config : uninitializedConfigs)
            configs.add(config.getConfig());
        uninitializedConfigs = null;
    }

    public void addConfig(LPCConfig config){
        if(uninitializedConfigs != null) uninitializedConfigs.add(config);
        if(configs != null) configs.add(config.getConfig());
    }

    public void addHotkeyConfig(String name, String defaultStorageString, String translationPrefix, IHotkeyCallback callBack){
        addConfig(new HotkeyConfig(
                parent, name, defaultStorageString,
                translationPrefix,
                callBack));
    }

    public void addConfigOpenGuiConfig(String defaultStorageString){
        addConfig(new ConfigOpenGuiConfig(parent, defaultStorageString));
    }

    public String getTranslationKey(){
        return translationKey;
    }

    public String getDisplayName(){
        return StringUtils.translate(this.translationKey);
    }
}
