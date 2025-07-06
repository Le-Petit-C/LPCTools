package lpctools.mixin.client.HappyGhastRidingTweak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static lpctools.tweaks.HappyGhastRidingTweak.*;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PlayerInput;equals(Ljava/lang/Object;)Z"))
    boolean shouldUpdateTest(boolean lastValue){
        if(lastModified){
            lastModified = false;
            return true;
        }
        if(!happyGhastRidingTweak.getAsBoolean()) return lastValue;
        if(!(MinecraftClient.getInstance().player instanceof ClientPlayerEntity player)) return lastValue;
        if(!(player.getVehicle() instanceof HappyGhastEntity)) return lastValue;
        if(!happyGhastDismountKey.getKeybind().isPressed()) return lastValue;
        lastModified = true;
        return false;
    }
    @Unique boolean lastModified = false;
}
