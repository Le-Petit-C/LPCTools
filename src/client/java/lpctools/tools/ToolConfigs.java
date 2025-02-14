package lpctools.tools;

import lpctools.lpcfymasaapi.IConfigListCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.tools.fillingassistant.FillingAssistant;
import org.jetbrains.annotations.NotNull;

public class ToolConfigs {
    public static LPCConfigList tools;
    public static void init(@NotNull LPCConfigPage page){
        tools = page.addList("tools");
        tools.setCallback(callback);
        FillingAssistant.init(tools);
    }

    private static final Callback callback = new Callback();
    private static class Callback implements IConfigListCallback{
        @Override
        public void onListRefresh() {
            FillingAssistant.refresh();
        }
    }
}
