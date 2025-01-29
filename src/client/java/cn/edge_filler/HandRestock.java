package cn.edge_filler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class HandRestock {
    public static void restock(){
        if(!Data.shouldplace) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        PlayerInventory inventory = player.getInventory();
        if(!inventory.getMainHandStack().isEmpty() && inventory.getMainHandStack().getItem() == Data.mainHandContainedItem) return;
        int i;
        for (i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Data.mainHandContainedItem) {
                ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
                if(itm == null) break;
                if(i < 9){
                    inventory.selectedSlot = i;
                    inventory.markDirty();
                }
                else{
                    int syncId = player.currentScreenHandler.syncId;
                    itm.clickSlot(syncId, i, inventory.selectedSlot, SlotActionType.SWAP, player);
                }
                break;
            }
        }
        if(i >= inventory.size())
            Data.switchPlaceMode();
    }
}
