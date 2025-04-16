package lpctools.tools.singletool;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;

public class SingleTool {
    public static BooleanConfig slightXRay;
    public static void init(ThirdListConfig STConfig){
        slightXRay = STConfig.addBooleanConfig("slightXRay", false, new SlightXRay());
    }
}
