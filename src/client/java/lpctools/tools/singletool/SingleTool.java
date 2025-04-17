package lpctools.tools.singletool;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;

public class SingleTool {
    public static BooleanHotkeyConfig slightXRay;
    public static void init(ThirdListConfig STConfig){
        slightXRay = STConfig.addBooleanHotkeyConfig("slightXRay", false, null, new SlightXRay());
    }
}
