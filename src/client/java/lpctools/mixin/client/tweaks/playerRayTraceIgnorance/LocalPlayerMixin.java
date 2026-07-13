package lpctools.mixin.client.tweaks.playerRayTraceIgnorance;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tweaks.PlayerCrosshairFilter.isLocalPlayerGettingHitResult;

@Mixin(GameRenderer.class) public class LocalPlayerMixin {
	@Inject(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("HEAD"))
	void injectPickHead(Entity entity, double d, double e, float f, CallbackInfoReturnable<HitResult> cir) {
		isLocalPlayerGettingHitResult = true;
	}

	@Inject(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At("RETURN"))
	void injectPickReturn(Entity entity, double d, double e, float f, CallbackInfoReturnable<HitResult> cir) {
		isLocalPlayerGettingHitResult = false;
	}
}
