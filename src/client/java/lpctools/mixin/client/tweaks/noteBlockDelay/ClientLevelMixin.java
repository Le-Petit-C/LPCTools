package lpctools.mixin.client.tweaks.noteBlockDelay;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpctools.tweaks.NoteBlockDelay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@WrapOperation(method = "playSound",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"))
	SoundEngine.PlayResult modifyMarkedNoteBlockSound(
		SoundManager soundManager, SoundInstance instance, Operation<SoundEngine.PlayResult> original
	) {
		ClientLevel level = (ClientLevel)(Object)this;
		// 音高必须在调度延迟【之前】就改好：被接手的那次播放复用的就是这个实例本体，
		// 而且之后不会再经过这里（flushSound 是直接调 SoundManager.play 的）
		NoteBlockDelay.tryApplyPitch(level, instance);
		if (NoteBlockDelay.tryScheduleDelay(level, instance, 0.0))
			// 这里报 STARTED 而不是 NOT_STARTED：我们确实把这次播放「接手」了，
			// 声音稍后会由 SoundManager.play 正常走完整流程（字幕、channel、增益都在那时才做）。
			// 报 NOT_STARTED 的话，万一将来有人（或别的 mod 照这条路抄）把它当「失败了，试试别的」的依据，
			// 就会造成重复播放；STARTED 是惰性的，最坏也只是「以为它已经开始播了」。
			return SoundEngine.PlayResult.STARTED;
		else return original.call(soundManager, instance);
	}

	/**
	 * vanilla 的 {@code playSound} 只在 {@code distanceDelay} 为 true 时才走这条分支，
	 * 而音符盒音效来自 {@code ClientboundSoundPacket} → {@code playSeededSound(..., false, seed)}，
	 * 所以它实际上走不到；写出来是为了让「所有让声音响起来的出口」都被守住，
	 * 以免将来调用方式变化时功能静默失效。
	 */
	@WrapOperation(method = "playSound",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V"))
	void modifyMarkedNoteBlockDelayedSound(
		SoundManager soundManager, SoundInstance instance, int delay, Operation<Void> original
	) {
		ClientLevel level = (ClientLevel)(Object)this;
		// 与上面同理：先把音高改到实例上，再决定是接手延迟还是原样放行
		NoteBlockDelay.tryApplyPitch(level, instance);
		if (NoteBlockDelay.tryScheduleDelay(level, instance, delay)) return;
		original.call(soundManager, instance, delay);
	}
}
