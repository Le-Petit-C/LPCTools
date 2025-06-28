package lpctools.lpcfymasaapi.interfaces.data;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import org.jetbrains.annotations.NotNull;

public class LPCConfigData {
    public final @NotNull ILPCConfigList parent;
    public final boolean hasHotkey;
    public String translatedName = "";
    public LPCConfigData(@NotNull ILPCConfigList parent, boolean hasHotkey){
        this.parent = parent;
        this.hasHotkey = hasHotkey;
    }
}
