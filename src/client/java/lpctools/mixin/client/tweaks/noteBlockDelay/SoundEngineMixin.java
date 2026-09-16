package lpctools.mixin.client.tweaks.noteBlockDelay;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpctools.tweaks.NoteBlockDelay;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@WrapOperation(method = "calculatePitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	float wrapPitchClamp(float value, float min, float max, Operation<Float> original) {
		if(NoteBlockDelay.disablePitchClamp.getBooleanValue()) return value;
		else return original.call(value, min, max);
	}
}
