package lpctools.mixin.client;

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

@Mixin(PlayerInputC2SPacket.class)
public class PlayerInputC2SPacketMixin {
    @Shadow @Final @Mutable
    private PlayerInput input;
    @Inject(method = "<init>", at = @At("TAIL"))
    void initInject(PlayerInput playerInput, CallbackInfo ci){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null || !(player.getVehicle() instanceof HappyGhastEntity)) return;
        input = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), input.jump(), false, input.sprint());
    }
}
