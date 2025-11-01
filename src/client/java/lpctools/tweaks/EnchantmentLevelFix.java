package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;

import java.util.HashMap;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addIntegerConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class EnchantmentLevelFix {
	public static final BooleanThirdListConfig enchantmentLevelFix = new BooleanThirdListConfig(TweakConfigs.tweaks, "enchantmentLevelFix", false, EnchantmentLevelFix::clearCache);
	static {listStack.push(enchantmentLevelFix);}
	public static final IntegerConfig romanNumeralMaxLevel = addIntegerConfig("romanNumeralMaxLevel", 32, 0, 3999, EnchantmentLevelFix::clearCache);
	static {listStack.pop();}
	
	public static final HashMap<String, String> cachedRomanNumerals = new HashMap<>();
	public static final HashMap<String, String> suppressedRomanNumerals = new HashMap<>();
	public static final String ENCHANTMENT_LEVEL_PREFIX = "enchantment.level.";
	
	private static void clearCache(){
		cachedRomanNumerals.clear();
		suppressedRomanNumerals.clear();
		for(int i = romanNumeralMaxLevel.getAsInt() + 1; i <= 10; i++)
			suppressedRomanNumerals.put(ENCHANTMENT_LEVEL_PREFIX + i, String.valueOf(i));
	}
}
