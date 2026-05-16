package lpctools.mixin.client.tweaks.happyGhastRidingTweak;

import lpctools.tweaks.HappyGhastRidingTweak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread;

@Mixin(ServerboundPlayerInputPacket.class)
public class PlayerInputC2SPacketMixin {
    @Shadow @Final @Mutable
    private Input input;
    @Inject(method = "<init>", at = @At("TAIL"))
    void initInject(Input playerInput, CallbackInfo ci){
        if(!isOnRenderThread()) return;
        if(!HappyGhastRidingTweak.happyGhastRidingTweak.getBooleanValue()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null || !(player.getVehicle() instanceof HappyGhast)) return;
        input = new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), HappyGhastRidingTweak.happyGhastDismountKey.getKeybind().isPressed(), input.sprint());
    }
}
