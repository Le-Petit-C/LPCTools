package lpctools.mixin.client.tweaks.playerRayTraceIgnorance;

import lpctools.tweaks.PlayerCrosshairFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

import static lpctools.tweaks.PlayerCrosshairFilter.isLocalPlayerGettingHitResult;

@Mixin(ProjectileUtil.class) public class ProjectileUtilMixin {
	@Inject(method = "getEntityHitResult*", at = @At("HEAD"), cancellable = true)
	private static void injectGetEntityHitResultHead(CallbackInfoReturnable<Collection<EntityHitResult>> cir) {
		if (isLocalPlayerGettingHitResult && PlayerCrosshairFilter.passThroughEntities.getBooleanValue() && Minecraft.getInstance().isSameThread())
			cir.setReturnValue(null);
	}
	@Inject(method = "getManyEntityHitResult*", at = @At("HEAD"), cancellable = true)
	private static void injectGetManyEntityHitResultHead(CallbackInfoReturnable<Collection<EntityHitResult>> cir) {
		if (isLocalPlayerGettingHitResult && PlayerCrosshairFilter.passThroughEntities.getBooleanValue() && Minecraft.getInstance().isSameThread())
			cir.setReturnValue(new ArrayList<>());
	}
}
