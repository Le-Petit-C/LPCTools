package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

//配置列表
public class LPCConfigList implements ILPCConfigList {
    public LPCConfigList(LPCConfigPage page, String translationKey){
        this.page = page;
        this.translationKey = translationKey;
    }
    public boolean hasHotkeyConfig() {
        for(ILPCConfig config : getConfigs())
            if(config.hasHotkey()) return true;
        return false;
    }
    @Override public @NotNull String getNameKey(){return translationKey;}
    @Override public @NotNull LPCConfigPage getPage() {return page;}
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs;}

    private final String translationKey;
    private final LPCConfigPage page;
    private final ArrayList<ILPCConfig> subConfigs = new ArrayList<>();
}
