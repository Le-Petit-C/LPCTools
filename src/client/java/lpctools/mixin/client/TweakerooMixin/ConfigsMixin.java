package lpctools.mixin.client.TweakerooMixin;

import fi.dy.masa.tweakeroo.config.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tweaks.LPCfyTweakerooList.*;

@Pseudo @Mixin(value = Configs.class, remap = false)
public class ConfigsMixin {
	@Inject(method = "loadFromFile", at = @At("HEAD"), remap = false)
	private static void injectLoadFromFileHead(CallbackInfo ci){
		if(lpcfyTweakerooList.getAsBoolean() && functionClass != null)
			functionClass.updateConfigs(false);
	}
	@Inject(method = "loadFromFile", at = @At("RETURN"), remap = false)
	private static void injectLoadFromFileReturn(CallbackInfo ci){
		if(lpcfyTweakerooList.getAsBoolean() && functionClass != null){
			functionClass.updateConfigs(true);
			functionClass.refreshCachedConfigs();
		}
	}
	@Inject(method = "saveToFile", at = @At("HEAD"), remap = false)
	private static void injectSaveToFileHead(CallbackInfo ci){
		if(lpcfyTweakerooList.getAsBoolean() && functionClass != null)
			functionClass.updateConfigs(false);
	}
	@Inject(method = "saveToFile", at = @At("RETURN"), remap = false)
	private static void injectSaveToFileReturn(CallbackInfo ci){
		if(lpcfyTweakerooList.getAsBoolean() && functionClass != null)
			functionClass.updateConfigs(true);
	}
}
