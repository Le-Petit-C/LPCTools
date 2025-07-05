package lpctools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread;

@Mixin(HappyGhastEntity.class)
public class HappyGhastEntityMixin {
    @Redirect(method = "getControlledMovementInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getPitch()F"))
    float setPitch(PlayerEntity instance){
        if(!isOnRenderThread()) return instance.getPitch();
        return 0;
    }
    @ModifyArg(index = 1, method = "getControlledMovementInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;<init>(DDD)V"))
    double isSneaking(double h, @Local(argsOnly = true) PlayerEntity controllingPlayer){
        if(!isOnRenderThread()) return h;
        if(controllingPlayer.isSneaking()) return h - 0.5 ;
        else return h;
    }
}
