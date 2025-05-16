package lpctools.lpcfymasaapi.implementations.data;

import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import org.jetbrains.annotations.NotNull;

public class LPCConfigData {
    public final @NotNull ILPCConfigList parent;
    public final boolean hasHotkey;
    public LPCConfigData(@NotNull ILPCConfigList parent, boolean hasHotkey){
        this.parent = parent;
        this.hasHotkey = hasHotkey;
    }
}
