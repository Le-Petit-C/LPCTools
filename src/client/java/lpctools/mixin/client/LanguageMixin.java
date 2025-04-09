package lpctools.mixin.client;

import lpctools.util.LanguageExtra;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Language.class)
public abstract class LanguageMixin {
    @Shadow public abstract String get(String key);
    @Inject(method = "get(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    void mixinGet(String key, CallbackInfoReturnable<String> cir){
        if(LanguageExtra.redirectContainsKey(key))
            cir.setReturnValue(get(LanguageExtra.redirectGet(key)));
    }
}
