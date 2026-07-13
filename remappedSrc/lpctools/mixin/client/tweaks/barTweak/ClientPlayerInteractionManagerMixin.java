package lpctools.mixin.client.tweaks.barTweak;

import lpctools.tweaks.BarTweaks;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow private GameType localPlayerMode;
    @Inject(method = "hasExperience", at = @At("RETURN"), cancellable = true)
    void hasExperienceBarReturn(CallbackInfoReturnable<Boolean> cir){
        if(BarTweaks.creativeShowsExperienceBar.getAsBoolean() && localPlayerMode.isCreative()) cir.setReturnValue(true);
    }
    @Inject(method = "canHurtPlayer", at = @At("RETURN"), cancellable = true)
    void hasStatusBarsReturn(CallbackInfoReturnable<Boolean> cir){
        if(BarTweaks.creativeShowsStatusBar.getAsBoolean() && localPlayerMode.isCreative()) cir.setReturnValue(true);
    }
}
