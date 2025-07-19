package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addHotkeyConfig;

@SuppressWarnings("unused")
public class HappyGhastRidingTweak {
    public static final BooleanThirdListConfig happyGhastRidingTweak = new BooleanThirdListConfig(TweakConfigs.tweaks, "happyGhastRidingTweak", false, null);
    public static final HotkeyConfig happyGhastDismountKey = addHotkeyConfig(happyGhastRidingTweak, "happyGhastDismountKey", null);
}
