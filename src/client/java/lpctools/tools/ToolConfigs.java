package lpctools.tools;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.tools.fillingassistant.FillingAssistant;
import org.jetbrains.annotations.NotNull;

public class ToolConfigs {
    public static void init(@NotNull LPCConfigPage page){
        LPCConfigList tools = page.addList("tools");
        tools.addHotkeyConfig("edgeFiller", "", FillingAssistant.getHotkeyCallback());
        tools.addStringListConfig("fillingAssistantPlaceableBlocks", FillingAssistant.defaultPlaceableItemIdList, FillingAssistant.getPlaceableItemsChangeCallback());
    }
}
