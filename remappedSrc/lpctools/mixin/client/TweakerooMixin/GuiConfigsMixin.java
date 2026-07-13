package lpctools.mixin.client.TweakerooMixin;

import fi.dy.masa.tweakeroo.gui.GuiConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tweaks.LPCfyTweakerooList.FunctionClass.*;

@Pseudo @Mixin(value = GuiConfigs.class, remap = false)
public class GuiConfigsMixin {
	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	void injectInitReturn(CallbackInfo ci){
		tweakerooGuiConfigs = (GuiConfigs)(Object)this;
	}
}
