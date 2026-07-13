package lpctools.lpcfymasaapi.interfaces;

import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;

public interface ILPCDefaultConfigStyle extends ILPCConfig{
    LPCConfigData getLPCConfigData();

    default @Override @NotNull ILPCConfigReadable getParent(){return getLPCConfigData().parent;}
    default @Override boolean hasHotkey(){return getLPCConfigData().hasHotkey;}
}
