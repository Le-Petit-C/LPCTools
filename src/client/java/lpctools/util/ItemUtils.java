package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

@SuppressWarnings("unused")
public class ItemUtils {
    public static void swapHands(){clickSlot(-1, 40, SlotActionType.SWAP);}
    public static void swapSlotWithOffhand(int slot){clickSlot(slot, 40, SlotActionType.SWAP);}
    public static void swapSlotWithMainhand(int slot){
        if(slot == 40) swapHands();
        else clickSlot(slot, -1, SlotActionType.SWAP);
    }
    public static void swapSlotWithHotbar(int slot, int hotbar){clickSlot(slot, hotbar, SlotActionType.SWAP);}
    /*
    type = SlotActionType.SWAP时:
        slot:
            -1:自动转换为主手
            0~8:自动转换为对应快捷栏
            9~35:背包栏
            36~44:快捷栏
        button:
            -1:自动转换为主手
            0~8:快捷栏
            40:副手
    */
    public static void clickSlot(int slot, int button, SlotActionType type){
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(itm == null) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        if(slot == -1) slot = player.getInventory().selectedSlot;
        if(button == -1) button = player.getInventory().selectedSlot;
        if(slot < 9) slot += 36;
        itm.clickSlot(player.currentScreenHandler.syncId, slot, button, type, player);
    }
    public static ItemStack getOffhandStack(PlayerInventory inventory){
        return inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
    }
}
