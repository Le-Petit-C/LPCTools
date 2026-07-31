package lpctools.mixin.client.utils.playerRotManaging;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpctools.util.mixin.PlayerRotManaging;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Shadow private float yRotLast;
	@Shadow private float xRotLast;
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V"))
	void wrapSendPositionInTick(LocalPlayer instance, Operation<Void> original) {
		PlayerRotManaging.wrapSendPositionInTick(instance, original, yRotLast, xRotLast);
	}
}
