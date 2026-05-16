package lpctools.mixin.client;

import lpctools.debugs.DebugConfigs;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class DisplayClickSlotArguments {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    void displayArguments(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo ci){
        if(!DebugConfigs.displayClickSlotArguments.getAsBoolean()) return;
        player.displayClientMessage(Component.nullToEmpty(String.format("clickSlot(%d, %d, %d, %s, %s)", syncId, slotId, button, actionType.toString(), player.getName().getString())), false);
    }
}
