package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class EnchantmentLevelFix {
	public static final BooleanThirdListConfig enchantmentLevelFix = new BooleanThirdListConfig(TweakConfigs.tweaks, "enchantmentLevelFix", false, null);
	static {listStack.push(enchantmentLevelFix);}
	public static final IntegerConfig maxFixedLevel = addIntegerConfig("maxFixedLevel", 10);
	static {listStack.pop();}
}
