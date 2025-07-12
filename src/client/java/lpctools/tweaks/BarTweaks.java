package lpctools.tweaks;

import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class BarTweaks {
    public static final ThirdListConfig barTweaks = new ThirdListConfig(TweakConfigs.tweaks, "barTweaks", null);
    static {listStack.push(barTweaks);}
    public static final BooleanConfig expBarDisplaysLocatorPoints = addBooleanConfig("expBarDisplaysLocatorPoints", false);
    public static final BooleanConfig jumpBarDisplaysLocatorPoints = addBooleanConfig("jumpBarDisplaysLocatorPoints", false);
    public static final BooleanConfig locatorBarUsesExpBackground = addBooleanConfig("locatorBarUsesExpBackground", false);
    public static final BooleanConfig creativeShowsExperienceBar = addBooleanConfig("creativeShowsExperienceBar", false);
    public static final BooleanConfig creativeShowsStatusBar = addBooleanConfig("creativeShowsStatusBar", false);
    static {listStack.pop();}
}
