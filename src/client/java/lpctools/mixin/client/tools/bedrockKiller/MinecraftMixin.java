package lpctools.mixin.client.tools.bedrockKiller;

import lpctools.tools.bedrockKiller.leakPreventer.BedrockKiller;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("RETURN"))
	void onStartAttackReturn(CallbackInfoReturnable<Boolean> cir) {
		BedrockKiller.operate();
	}
}
