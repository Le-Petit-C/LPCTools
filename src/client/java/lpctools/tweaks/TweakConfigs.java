package lpctools.tweaks;

import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;

public class TweakConfigs {
    public static final LPCConfigList tweaks = new LPCConfigList(LPCTools.page, "tweaks");
    public static final BooleanConfig modMenuPlayClickSound = new BooleanConfig(tweaks, "modMenuPlayClickSound", false);
    static {
        tweaks.addConfigs(
            BlockReplaceHotkey.blockReplaceHotkey,
            HappyGhastRidingTweak.happyGhastRidingTweak,
            BarTweaks.barTweaks,
            EnchantmentLevelFix.enchantmentLevelFix,
            modMenuPlayClickSound
        );
    }
}
