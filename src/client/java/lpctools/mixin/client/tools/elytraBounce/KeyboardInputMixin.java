package lpctools.mixin.client.tools.elytraBounce;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpctools.tools.elytraBounce.ElytraBounce;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {
	@WrapOperation(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
	Input modifyInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint, Operation<Input> original) {
		return ElytraBounce.modifyInput(forward, backward, left, right, jump, shift, sprint, original);
	}
}
