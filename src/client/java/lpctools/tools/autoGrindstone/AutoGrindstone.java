package lpctools.tools.autoGrindstone;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;

public class AutoGrindstone {
    public static final ImmutableList<String> defaultStrings = ImmutableList.of(
            "minecraft:soul_speed; 2"
    );
    public static BooleanHotkeyConfig autoGrindstoneConfig;
    public static StringListConfig limitEnchantmentsConfig;
    public static void init(){
        autoGrindstoneConfig = addBooleanHotkeyConfig("autoGrindstone", false, null);
        setLPCToolsToggleText(autoGrindstoneConfig);
        limitEnchantmentsConfig = addStringListConfig("limitEnchantments", defaultStrings);
    }
}
