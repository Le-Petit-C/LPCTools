package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ItemUtils {
    static void swapHands(){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        PlayerInventory inventory = player.getInventory();
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if(networkHandler != null){
            networkHandler.sendPacket(
                    new PlayerActionC2SPacket(net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN)
            );
            ItemStack stack = player.getOffHandStack();
            inventory.offHand.set(0, inventory.getMainHandStack());
            inventory.setStack(inventory.selectedSlot, stack);
        }
    }
    static void swapSlotWithHotbar(int slot, int hotbar){
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(itm == null) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        itm.clickSlot(player.currentScreenHandler.syncId, slot, hotbar, SlotActionType.SWAP, player);
    }
}
