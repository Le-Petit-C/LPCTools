package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class HandRestock {
    //从背包里寻找第一个满足条件的物品槽索引，主手槽位先于一般槽检测。没找到则返回-1，找到副手返回-2
    //offhandPriority:副手槽位的检测优先级，-1表示在主手之前，0表示在主手之后但是在其他槽位之前，1表示在所有槽位之后，其他表示不检测
    public static int search(Set<Item> restockItems, int offhandPriority){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return -1;
        PlayerInventory inventory = player.getInventory();
        if(offhandPriority == -1 && isStackOk(inventory.offHand.getFirst(), restockItems)) return -2;
        if(isStackOk(inventory.getMainHandStack(), restockItems)) return inventory.selectedSlot;
        if(offhandPriority == 0 && isStackOk(inventory.offHand.getFirst(), restockItems)) return -2;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(isStackOk(stack, restockItems)) return i;
        }
        if(offhandPriority == 1 && isStackOk(inventory.offHand.getFirst(), restockItems)) return -2;
        return -1;
    }
    //从背包里寻找集合中的物品并将其换到主手，成功使主手拿上给定物品返回true，失败返回false
    //offhandPriority与search中的offhandPriority同义，进一步地，如果offhandPriority=-1即副手优先级比主手高，则变成填充副手而不是主手
    //restockOffhand表示是否填充副手
    public static boolean restock(Set<Item> restockItems, int offhandPriority){
        int i = search(restockItems, offhandPriority);
        if(i == -1) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        PlayerInventory inventory = player.getInventory();
        if(inventory == null) return false;
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(itm == null) return false;
        if(offhandPriority == -1){
            if(i != -2){
                ItemUtils.swapSlotWithHotbar(i, inventory.selectedSlot);
                ItemUtils.swapHands();
                ItemUtils.swapSlotWithHotbar(i, inventory.selectedSlot);
            }
        }
        else{
            if(i == -2) ItemUtils.swapHands();
            else if(i < 9) inventory.selectedSlot = i;
            else ItemUtils.swapSlotWithHotbar(i, inventory.selectedSlot);
        }
        return true;
    }

    private static boolean isStackOk(ItemStack stack, Set<Item> restockItems){
        if(stack.isEmpty()) return false;
        return restockItems.contains(stack.getItem());
    }
}
