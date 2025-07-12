package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.gui.GuiConfigsBase;

import java.util.ArrayList;

public interface ILPCConfigReadable extends ILPCConfigBase{
    ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList);
    static ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    defaultBuildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList, Iterable<? extends ILPCConfig> configs, boolean needAlign){
        for(ILPCConfig config : configs){
            config.refreshName(needAlign);
            wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
            if(config instanceof ILPCConfigReadable list)
                list.buildConfigWrappers(wrapperList);
        }
        return wrapperList;
    }
}
