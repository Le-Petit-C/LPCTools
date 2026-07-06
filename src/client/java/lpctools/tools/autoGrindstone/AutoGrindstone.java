package lpctools.tools.autoGrindstone;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.StringListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.autoGrindstone.AutoGrindstoneData.*;

public class AutoGrindstone {
    public static final BooleanHotkeyThirdListConfig AGConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "AG", AutoGrindstone::onToolToggled);
    static {setLPCToolsToggleText(AGConfig);}
    static {listStack.push(AGConfig);}
    public static final StringListConfig limitEnchantmentsConfig = addStringListConfig("limitEnchantments", defaultStrings);
    static {listStack.pop();}
    private static void onToolToggled() {
        if(AGConfig.getBooleanValue()) {
            if(runner == null) runner = new AutoGrindstoneRunner();
            Registries.ON_SCREEN_CHANGED.register(runner);
        }
        else {
            if(runner != null) Registries.ON_SCREEN_CHANGED.unregister(runner);
            runner = null;
        }
    }
}
