package lpctools.tweaks;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addHotkeyConfig;

@SuppressWarnings("unused")
public class HappyGhastRidingTweak {
    public static final ThirdListConfig happyGhastRidingTweak = new ThirdListConfig(TweakConfigs.tweaks, "happyGhastRidingTweak", false);
    public static final HotkeyConfig happyGhastDismountKey = addHotkeyConfig(happyGhastRidingTweak, "happyGhastDismountKey", null);
}
