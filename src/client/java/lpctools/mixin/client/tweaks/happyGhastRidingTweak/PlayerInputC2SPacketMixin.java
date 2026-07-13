package lpctools.mixin.client.tweaks.happyGhastRidingTweak;

import lpctools.tweaks.HappyGhastRidingTweak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.animal.HappyGhast;
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
    void initInject(Input input, CallbackInfo ci){
        if(!isOnRenderThread()) return;
        if(!HappyGhastRidingTweak.happyGhastRidingTweak.getBooleanValue()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null || !(player.getVehicle() instanceof HappyGhast)) return;
        this.input = new Input(this.input.forward(), this.input.backward(), this.input.left(), this.input.right(), this.input.jump(), HappyGhastRidingTweak.happyGhastDismountKey.getKeybind().isPressed(), this.input.sprint());
    }
}
