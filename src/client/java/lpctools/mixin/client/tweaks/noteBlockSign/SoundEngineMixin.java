package lpctools.mixin.client.tweaks.noteBlockSign;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lpctools.tweaks.NoteBlockSignTweaks;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@WrapOperation(method = "calculatePitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	float wrapPitchClamp(float value, float min, float max, Operation<Float> original) {
		if(NoteBlockSignTweaks.disablePitchClamp.getBooleanValue()) return value;
		else return original.call(value, min, max);
	}

	@WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
	float scaleFinalVolume(SoundEngine instance, float volume, SoundSource source, Operation<Float> original, @Local(argsOnly = true) SoundInstance soundInstance) {
		float result = original.call(instance, volume, source);
		Double multiplier = NoteBlockSignTweaks.getVolumeMultiplier(soundInstance);
		if(multiplier == null) return result;
		else return (float)(result * multiplier);
	}
}
