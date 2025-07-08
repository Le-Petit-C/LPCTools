package lpctools.tools.autoGrindstone;

import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.autoGrindstone.AutoGrindstoneData.*;

public class AutoGrindstone {
    public static final BooleanHotkeyThirdListConfig AGConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "AG", false, false, null, null, false);
    static {setLPCToolsToggleText(AGConfig);}
    static {listStack.push(AGConfig);}
    public static final StringListConfig limitEnchantmentsConfig = addStringListConfig("limitEnchantments", defaultStrings);
    static {listStack.pop();}
}
