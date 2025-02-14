package lpctools.generic;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.ConfigOpenGuiConfig;
import org.jetbrains.annotations.NotNull;

public class GenericConfigs {
    public static void init(@NotNull LPCConfigPage page){
        LPCConfigList generic = page.addList("generic");
        configOpenGuiConfig = generic.addConfigOpenGuiConfig("Z,C");
    }

    static ConfigOpenGuiConfig configOpenGuiConfig;
}
