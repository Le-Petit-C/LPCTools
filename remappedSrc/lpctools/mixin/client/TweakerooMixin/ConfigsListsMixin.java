package lpctools.mixin.client.TweakerooMixin;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.tweakeroo.config.Configs;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tweaks.LPCfyTweakerooList.FunctionClass.optionSetter;

@Pseudo @Mixin(value = Configs.Lists.class, remap = false)
public class ConfigsListsMixin {
	@Shadow(remap = false) @Final @Mutable
	public static ImmutableList<IConfigBase> OPTIONS;
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onClassInitTail(CallbackInfo ci){
		optionSetter = list -> OPTIONS = list;
	}
}
