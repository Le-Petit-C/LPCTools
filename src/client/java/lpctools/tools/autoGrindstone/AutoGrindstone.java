package lpctools.tools.autoGrindstone;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class AutoGrindstone {
    public static final ImmutableList<String> defaultStrings = ImmutableList.of(
            "minecraft:soul_speed; 2"
    );
    public static BooleanHotkeyConfig autoGrindstoneConfig;
    public static StringListConfig limitEnchantmentsConfig;
    public static void init(){
        autoGrindstoneConfig = addBooleanHotkeyConfig("autoGrindstone", false, null);
        autoGrindstoneConfig.getKeybind().setCallback((action, key)->{
            autoGrindstoneConfig.toggleBooleanValue();
            ToolConfigs.displayToggleMessage(autoGrindstoneConfig);
            return true;
        });
        limitEnchantmentsConfig = addStringListConfig("limitEnchantments", defaultStrings);
    }
}
