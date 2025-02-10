package lpctools.Generic;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;

public class GenericOptions{
    public static void init(@NotNull LPCConfigPage page){
        LPCConfigList generic = page.addList("generic");
        generic.addConfigOpenGuiConfig("Z,C");
    }
}
