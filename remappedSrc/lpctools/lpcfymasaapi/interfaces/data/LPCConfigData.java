package lpctools.lpcfymasaapi.interfaces.data;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;

public class LPCConfigData {
    public final @NotNull ILPCConfigReadable parent;
    public final boolean hasHotkey;
    public LPCConfigData(@NotNull ILPCConfigReadable parent, boolean hasHotkey){
        this.parent = parent;
        this.hasHotkey = hasHotkey;
    }
}
