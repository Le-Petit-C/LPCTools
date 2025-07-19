package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;

import java.util.ArrayList;

public interface IExpandableThirdList extends IThirdListBase{
    boolean isExpanded();
    void setExpanded(boolean expanded);
    String expandedKey = "expanded";
    default ILPCUniqueConfigBase.ButtonOption thirdListIconPreset(){return thirdListIconPreset(this);}
    static ILPCUniqueConfigBase.ButtonOption thirdListIconPreset(IExpandableThirdList config){
        return new ILPCUniqueConfigBase.ButtonOption(-1, (button, mouseButton)->config.setExpanded(!config.isExpanded()), null,
            ILPCUniqueConfigBase.iconButtonAllocator(config.isExpanded() ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER));
    }
    @Override default ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(isExpanded()) return IThirdListBase.super.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }
}
