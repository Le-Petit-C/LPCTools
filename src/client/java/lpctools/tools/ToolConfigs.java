package lpctools.tools;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.tools.fillingassistant.FillingAssistant;
import org.jetbrains.annotations.NotNull;

public class ToolConfigs {
    public static LPCConfigList tools;
    public static void init(@NotNull LPCConfigPage page){
        tools = page.addList("tools");
        FillingAssistant.init(tools);
    }
}
