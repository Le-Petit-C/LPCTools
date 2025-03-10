package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Set;

public class HandRestock {
    //从背包里寻找第一个满足条件的物品槽索引，主手槽位始终最优先检测。没找到则返回-1
    public static int search(Set<Item> restockItems){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return -1;
        PlayerInventory inventory = player.getInventory();
        if(isStackOk(inventory.getMainHandStack(), restockItems)) return inventory.selectedSlot;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(isStackOk(stack, restockItems)) return i;
        }
        return -1;
    }
    //从背包里寻找集合中的物品并将其换到主手，成功使主手拿上给定物品返回true，失败返回false
    public static boolean restock(Set<Item> restockItems){
        int i = search(restockItems);
        if(i == -1) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        PlayerInventory inventory = player.getInventory();
        if(inventory == null) return false;
        if(i < 9) inventory.selectedSlot = i;
        else {
            ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
            if(itm == null) return false;
            itm.clickSlot(player.currentScreenHandler.syncId, i, inventory.selectedSlot, SlotActionType.SWAP, player);
        }
        return true;
    }

    private static boolean isStackOk(ItemStack stack, Set<Item> restockItems){
        if(stack.isEmpty()) return false;
        return restockItems.contains(stack.getItem());
    }
}
