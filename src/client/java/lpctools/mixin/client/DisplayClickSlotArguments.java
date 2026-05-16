package lpctools.mixin.client;

import lpctools.debugs.DebugConfigs;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class DisplayClickSlotArguments {
    @Inject(method = "handleContainerInput", at = @At("HEAD"))
    void displayArguments(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci){
        if(!DebugConfigs.displayClickSlotArguments.getAsBoolean()) return;
        player.sendSystemMessage(Component.nullToEmpty(String.format("clickSlot(%d, %d, %d, %s, %s)", containerId, slotNum, buttonNum, containerInput.toString(), player.getName().getString())));
    }
}
