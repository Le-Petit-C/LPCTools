package lpctools.mixin.client.events;

import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.Registries.BEFORE_SCREEN_CHANGE;
import static lpctools.lpcfymasaapi.Registries.ON_SCREEN_CHANGED;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Inject(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/Minecraft;finishProfilers(ZLnet/minecraft/util/profiling/SingleTickProfiler;)V"), method = "run")
	void beforeEndTickDuration(CallbackInfo ci) {
		Registries.BETWEEN_RENDER_FRAMES.runner().betweenFrames();
	}
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	void injectSetScreenHead(Screen screen, CallbackInfo ci){
		if(BEFORE_SCREEN_CHANGE.runner().beforeScreenChange(screen))
			ci.cancel();
	}
	@Inject(method = "setScreen", at = @At("TAIL"))
	void injectSetScreenTail(Screen screen, CallbackInfo ci){
		ON_SCREEN_CHANGED.runner().run();
	}
}
