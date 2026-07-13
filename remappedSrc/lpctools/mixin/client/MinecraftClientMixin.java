package lpctools.mixin.client;

import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Inject(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/Minecraft;finishProfilers(ZLnet/minecraft/util/profiling/SingleTickProfiler;)V",
		shift = At.Shift.BEFORE), method = "run")
	void beforeEndTickDuration(CallbackInfo ci) {
		Registries.BETWEEN_RENDER_FRAMES.runner().betweenFrames();
	}
}
