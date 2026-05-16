package lpctools.mixin.client.tweaks.happyGhastRidingTweak;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.tweaks.HappyGhastRidingTweak;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread;

@Mixin(HappyGhast.class)
public class HappyGhastEntityMixin {
    @Redirect(method = "getRiddenInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getXRot()F"))
    float setPitch(Player instance){
        if(!isOnRenderThread()) return instance.getXRot();
        if(!HappyGhastRidingTweak.happyGhastRidingTweak.getBooleanValue()) return instance.getXRot();
        return 0;
    }
    @ModifyArg(index = 1, method = "getRiddenInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"))
    double isSneaking(double h, @Local(argsOnly = true) Player controllingPlayer){
        if(!isOnRenderThread()) return h;
        if(!HappyGhastRidingTweak.happyGhastRidingTweak.getBooleanValue()) return h;
        if(controllingPlayer.isShiftKeyDown()) return h - 0.5;
        else return h;
    }
}
