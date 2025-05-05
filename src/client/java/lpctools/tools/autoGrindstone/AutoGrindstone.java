package lpctools.tools.autoGrindstone;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.tools.ToolConfigs;

public class AutoGrindstone {
    public static final ImmutableList<String> defaultStrings = ImmutableList.of(
            "minecraft:soul_speed; 2"
    );
    public static BooleanHotkeyConfig autoGrindstoneConfig;
    public static StringListConfig limitEnchantmentsConfig;
    public static void init(ThirdListConfig AGConfig){
        autoGrindstoneConfig = AGConfig.addBooleanHotkeyConfig("autoGrindstone", false, null);
        autoGrindstoneConfig.getKeybind().setCallback((action, key)->{
            autoGrindstoneConfig.toggleBooleanValue();
            ToolConfigs.displayToggleMessage(autoGrindstoneConfig);
            return true;
        });
        limitEnchantmentsConfig = AGConfig.addStringListConfig("limitEnchantments", defaultStrings);
    }
}
