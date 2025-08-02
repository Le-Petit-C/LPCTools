package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.ToIntFunction;

public interface ILPCConfigReadable extends ILPCConfigBase, AutoCloseable{
    @NotNull Iterable<? extends ILPCConfig> getConfigs();
    default ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        int indent = 0;
        for(ILPCConfig config : getConfigs()){
            config.refreshName();
            wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
            if(config instanceof ILPCConfigReadable list)
                list.buildConfigWrappers(getStringWidth, wrapperList);
            indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
        }
        setAlignedIndent(indent);
        return wrapperList;
    }
    void setAlignedIndent(int indent);
    int getAlignedIndent();
    @Override default void close() throws Exception {
        for(ILPCConfig config : getConfigs()){
            if(config instanceof AutoCloseable closeable)
                closeable.close();
        }
    }
}
