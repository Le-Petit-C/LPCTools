package lpctools.tweaks;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TweakConfigs {
    public static LPCConfigList tweaks;
    public static void init(@NotNull LPCConfigPage page){
        tweaks = page.addList("tweaks");
    }
}
