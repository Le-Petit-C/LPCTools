package lpctools.mixin.client.enchantmentLevelFix;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tweaks.EnchantmentLevelFix.*;
import static lpctools.util.MathUtils.romanNumerals;

/*
* 目前的修复方案有一个问题
* 当游戏中存在大量无对应翻译内容的翻译键查询时
* 每次查询都会进行一次if(key.startsWith(ENCHANTMENT_LEVEL_PREFIX))的字符串前缀匹配操作
* 可能会带来一定的性能损失
* 但考虑到这种情况出现的频率应该不会很高，所以暂时不做额外优化
* 可能的优化方案：
* 在附魔相关类里面找到最终请求翻译的位置Mixin进去直接处理
*/

@Mixin(TranslationStorage.class)
public abstract class MixinTranslationStorage {
	@Shadow public abstract boolean hasTranslation(String key);
	@Inject(method = "get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
	private void mixinTranslateHead(String key, String fallback, CallbackInfoReturnable<String> cir){
		if(enchantmentLevelFix.getBooleanValue()){
			if(suppressedRomanNumerals.containsKey(key)){
				cir.setReturnValue(suppressedRomanNumerals.get(key));
				return;
			}
			if(hasTranslation(key)) return;
			if(cachedRomanNumerals.containsKey(key)){
				cir.setReturnValue(cachedRomanNumerals.get(key));
				return;
			}
			if(key.startsWith(ENCHANTMENT_LEVEL_PREFIX)){
				String levelStr = key.substring(ENCHANTMENT_LEVEL_PREFIX.length());
				int level;
				try{
					level = Integer.parseInt(levelStr);
				}catch (NumberFormatException ignored){
					return;
				}
				String res = level > romanNumeralMaxLevel.getAsInt() ? levelStr : romanNumerals(level);
				cachedRomanNumerals.put(key, res);
				cir.setReturnValue(res);
			}
		}
	}
}
