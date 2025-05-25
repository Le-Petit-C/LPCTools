package lpctools.mixin.client;

import lpctools.debugs.DebugConfigs;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class DisplayClickSlotArguments {
    @Inject(method = "clickSlot", at = @At("HEAD"))
    void displayArguments(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci){
        if(!DebugConfigs.displayClickSlotArguments.getAsBoolean()) return;
        player.sendMessage(Text.of(String.format("clickSlot(%d, %d, %d, %s, %s)", syncId, slotId, button, actionType.toString(), player.getName().getString())), false);
    }
}
