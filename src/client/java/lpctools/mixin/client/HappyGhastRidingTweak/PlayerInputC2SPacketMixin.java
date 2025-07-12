package lpctools.mixin.client.HappyGhastRidingTweak;

import lpctools.tweaks.HappyGhastRidingTweak;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread;

@Mixin(PlayerInputC2SPacket.class)
public class PlayerInputC2SPacketMixin {
    @Shadow @Final @Mutable
    private PlayerInput input;
    @Inject(method = "<init>", at = @At("TAIL"))
    void initInject(PlayerInput playerInput, CallbackInfo ci){
        if(!isOnRenderThread()) return;
        if(!HappyGhastRidingTweak.happyGhastRidingTweak.getBooleanValue()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null || !(player.getVehicle() instanceof HappyGhastEntity)) return;
        input = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), input.jump(), HappyGhastRidingTweak.happyGhastDismountKey.getKeybind().isPressed(), input.sprint());
    }
}
