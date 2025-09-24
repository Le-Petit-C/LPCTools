package lpctools.tweaks;

import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;

@SuppressWarnings("unused")
public class TweakConfigs {
    public static final LPCConfigList tweaks = new LPCConfigList(LPCTools.page, "tweaks");
    static {
        tweaks.addConfigs(
            BlockReplaceHotkey.blockReplaceHotkey,
            HappyGhastRidingTweak.happyGhastRidingTweak,
            BarTweaks.barTweaks,
            EnchantmentLevelFix.enchantmentLevelFix
        );
    }
}
