package lpctools.tweaks;

import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;

public class LocatorBarTweak {
    public static final BooleanConfig expBarDisplaysLocatorPoints = new BooleanConfig(TweakConfigs.tweaks, "expBarDisplaysLocatorPoints", false);
    public static final BooleanConfig jumpBarDisplaysLocatorPoints = new BooleanConfig(TweakConfigs.tweaks, "jumpBarDisplaysLocatorPoints", false);
    public static final BooleanConfig locatorBarUsesExpBackground = new BooleanConfig(TweakConfigs.tweaks, "locatorBarUsesExpBackground", false);
}
