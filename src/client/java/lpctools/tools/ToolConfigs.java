package lpctools.tools;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.ThirdListConfig;
import lpctools.tools.fillingassistant.FillingAssistant;
import lpctools.tools.liquidcleaner.LiquidCleaner;
import org.jetbrains.annotations.NotNull;

public class ToolConfigs {
    public static LPCConfigList tools;
    static ThirdListConfig FAConfig;
    static ThirdListConfig LCConfig;
    public static void init(@NotNull LPCConfigPage page){
        tools = page.addList("tools");
        FillingAssistant.init(FAConfig = tools.addThirdListConfig("FA", false));
        LiquidCleaner.init(LCConfig = tools.addThirdListConfig("LC", false));
    }
}
