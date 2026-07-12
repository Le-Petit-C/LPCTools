package lpctools.mixin.client.tweaks.happyGhastRidingTweak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static lpctools.tweaks.HappyGhastRidingTweak.*;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;equals(Ljava/lang/Object;)Z"))
    boolean shouldUpdateTest(boolean lastValue){
        if(lastModified){
            lastModified = false;
            return true;
        }
        if(!happyGhastRidingTweak.getBooleanValue()) return lastValue;
        if(!(Minecraft.getInstance().player instanceof LocalPlayer player)) return lastValue;
        if(!(player.getVehicle() instanceof HappyGhast)) return lastValue;
        if(!happyGhastDismountKey.getKeybind().isPressed()) return lastValue;
        lastModified = true;
        return false;
    }
    @Unique boolean lastModified = false;
}
