package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;

public interface IExpandableThirdList extends IThirdListBase{
    boolean isExpanded();
    void setExpanded(boolean expanded);
    String expandedKey = "expanded";
    default ILPCUniqueConfigBase.ButtonOption thirdListIconPreset(){return thirdListIconPreset(this);}
    static ILPCUniqueConfigBase.ButtonOption thirdListIconPreset(BooleanSupplier getExpandState, BooleanConsumer setExpandState){
        return new ILPCUniqueConfigBase.ButtonOption(-1, (button, mouseButton)->setExpandState.accept(!getExpandState.getAsBoolean()), null,
            ILPCUniqueConfigBase.iconButtonAllocator(getExpandState.getAsBoolean() ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER));
    }
    static ILPCUniqueConfigBase.ButtonOption thirdListIconPreset(IExpandableThirdList config){
        return thirdListIconPreset(config::isExpanded, config::setExpanded);
    }
    @Override default ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(isExpanded()) return IThirdListBase.super.buildConfigWrappers(getStringWidth, wrapperList);
        else return wrapperList;
    }
}
