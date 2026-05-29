package lpctools.mixin.client.tweaks.blockMiningCooldownTweaks;

import lpctools.tweaks.BlockBreakCooldownTweaks;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow private int destroyDelay;
	@Inject(method = "destroyBlock", at = @At("RETURN"))
	void injectDestroyBlockReturn(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if(BlockBreakCooldownTweaks.extraBlockBreakCooldown.getAsBoolean())
			destroyDelay = 5;
	}
	@Inject(method = "startDestroyBlock", at = @At("HEAD"))
	void injectStartDestroyBlockHead(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if(BlockBreakCooldownTweaks.startBreakBlockResetsBlockBreakCooldown.getAsBoolean())
			destroyDelay = 0;
	}
}
