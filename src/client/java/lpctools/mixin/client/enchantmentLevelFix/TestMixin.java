package lpctools.mixin.client.enchantmentLevelFix;

import lpctools.LPCTools;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Text.class)
public interface TestMixin {
	@Inject(method = "translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", at = @At("HEAD"))
	private static void mixinTranslateHead(String key, CallbackInfoReturnable<MutableText> cir){
		if(key.equals("enchantments.level.10")){
			LPCTools.LOGGER.info("ec10");
		}
	}
}
