package lpctools.mixin.client.tweaks.playerRayTraceIgnorance;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tweaks.PlayerCrosshairFilter.isLocalPlayerGettingHitResult;

@Mixin(LocalPlayer.class) public class LocalPlayerMixin {
	@Inject(method = "raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;", at = @At("HEAD"))
	void injectRayCastHitResultHead(float a, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
		isLocalPlayerGettingHitResult = true;
	}

	@Inject(method = "raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;", at = @At("RETURN"))
	void injectRayCastHitResultReturn(float a, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
		isLocalPlayerGettingHitResult = false;
	}
}
