package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

//配置列表
public class LPCConfigList implements ILPCConfigList{
    public @NotNull LPCConfigPage getPage(){return page;}
    public LPCConfigList(LPCConfigPage page, String translationKey){
        this.page = page;
        this.translationKey = translationKey;
    }
    @Override public String getNameKey(){return translationKey;}
    public boolean hasHotkeyConfig() {return hasHotkeyConfig;}
    @Override public @NotNull Iterable<ILPCConfig> getConfigs(){
        return configs;
    }
    @Override public <T extends ILPCConfig> T addConfig(T config){
        configs.add(config);
        if(config.hasHotkey())
            hasHotkeyConfig = true;
        return config;
    }

    private final @NotNull ArrayList<ILPCConfig> configs = new ArrayList<>();

    private final String translationKey;
    private final LPCConfigPage page;
    private boolean hasHotkeyConfig;
}
