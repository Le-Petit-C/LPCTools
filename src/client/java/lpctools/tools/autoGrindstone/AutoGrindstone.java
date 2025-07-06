package lpctools.tools.autoGrindstone;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.autoGrindstone.AutoGrindstoneData.*;

public class AutoGrindstone {
    public static final ThirdListConfig AGConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "AG", false);
    static {listStack.push(AGConfig);}
    public static final BooleanHotkeyConfig autoGrindstoneConfig = addBooleanHotkeyConfig("autoGrindstone", false, null);
    static {setLPCToolsToggleText(autoGrindstoneConfig);}
    public static final StringListConfig limitEnchantmentsConfig = addStringListConfig("limitEnchantments", defaultStrings);
    static {listStack.pop();}
}
