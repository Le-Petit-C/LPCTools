package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

//配置列表
public class LPCConfigList implements ILPCConfigList {
    public LPCConfigList(ILPCConfigBase parent, String nameKey){
        this.parent = parent;
        this.nameKey = nameKey;
    }
    public boolean hasHotkeyConfig() {
        for(ILPCConfig config : getConfigs())
            if(config.hasHotkey()) return true;
        return false;
    }

    @Override public @NotNull ILPCConfigBase getParent() {return parent;}
    @Override public @NotNull String getNameKey(){return nameKey;}
    @Override public @NotNull LPCConfigPage getPage() {return parent.getPage();}
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs;}

    private final String nameKey;
    private final ILPCConfigBase parent;
    private final ArrayList<ILPCConfig> subConfigs = new ArrayList<>();
}
