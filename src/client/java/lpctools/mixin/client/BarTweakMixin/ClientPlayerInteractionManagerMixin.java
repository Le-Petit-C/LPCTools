package lpctools.mixin.client.BarTweakMixin;

import lpctools.tweaks.BarTweaks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow private GameMode gameMode;
    @Inject(method = "hasExperienceBar", at = @At("RETURN"), cancellable = true)
    void hasExperienceBarReturn(CallbackInfoReturnable<Boolean> cir){
        if(BarTweaks.creativeShowsExperienceBar.getAsBoolean() && gameMode.isCreative()) cir.setReturnValue(true);
    }
    @Inject(method = "hasStatusBars", at = @At("RETURN"), cancellable = true)
    void hasStatusBarsReturn(CallbackInfoReturnable<Boolean> cir){
        if(BarTweaks.creativeShowsStatusBar.getAsBoolean() && gameMode.isCreative()) cir.setReturnValue(true);
    }
}
