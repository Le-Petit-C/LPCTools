package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HandRestock {
    public interface IRestockTest{
        boolean isStackOk(ItemStack stack);
    }
    public record SearchInSet(@NotNull Set<? extends Item> set) implements IRestockTest{
        @Override public boolean isStackOk(ItemStack stack){
            return set.contains(stack.getItem());
        }
    }
    //O(n)复杂度。。。但是这样是兼容性最好的方法
    public static int getHotbarStartSlotIndex(PlayerEntity player){
        ItemStack stack = player.getInventory().getStack(0);
        var slots = player.currentScreenHandler.slots;
        for (int i = 0; i < slots.size(); i++)
            if(slots.get(i).getStack() == stack)
                return i;
        //没找到则返回一个合理的默认值
        return slots.size() - (player.currentScreenHandler.syncId == 0 ? 9 : 10);
    }
    //从当前GUI界面所有槽里寻找第一个满足条件的物品槽索引，主手槽位先于一般槽检测。没找到则返回-1，找到则返回槽位索引，找到副手返回-2
    //offhandPriority:副手槽位的检测优先级，-1表示在主手之前，0表示在主手之后但是在其他槽位之前，1表示在所有槽位之后，其他表示不检测
    public static int search(IRestockTest restockTest, int offhandPriority){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return -1;
        PlayerInventory inventory = player.getInventory();
        var slots = player.currentScreenHandler.slots;
        if(offhandPriority == -1 && restockTest.isStackOk(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) return -2;
        if(restockTest.isStackOk(inventory.getSelectedStack())) return getHotbarStartSlotIndex(player) + inventory.getSelectedSlot();
        if(offhandPriority == 0 && restockTest.isStackOk(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) return -2;
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if(restockTest.isStackOk(slot.getStack())) return i;
        }
        if(offhandPriority == 1 && restockTest.isStackOk(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) return -2;
        return -1;
    }
    //从背包里寻找集合中的物品并将其换到手上，成功使目标手拿上给定物品返回拿到的物品数量，失败返回0
    //offhandPriority与search中的offhandPriority同义，进一步地，如果offhandPriority=-1即副手优先级比主手高，则变成填充副手而不是主手
    public static int restock(IRestockTest restockTest, int offhandPriority){
        int i = search(restockTest, offhandPriority);
        if(i == -1) return 0;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;
        var slots = player.currentScreenHandler.slots;
        PlayerInventory inventory = player.getInventory();
        if(inventory == null) return 0;
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(itm == null) return 0;
        int count;
        if(i == -2) count = inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getCount();
        else count = slots.get(i).getStack().getCount();
        if(offhandPriority == -1){
            if(i != -2) itm.clickSlot(player.currentScreenHandler.syncId, i, PlayerInventory.OFF_HAND_SLOT, SlotActionType.SWAP, player);
        }
        else{
            if(i == -2) itm.clickSlot(player.currentScreenHandler.syncId, getHotbarStartSlotIndex(player) + inventory.getSelectedSlot(), PlayerInventory.OFF_HAND_SLOT, SlotActionType.SWAP, player);
            else {
                int hotbarStart = getHotbarStartSlotIndex(player);
                if(i >= hotbarStart && i < hotbarStart + 9) inventory.setSelectedSlot(i - hotbarStart);
                else itm.clickSlot(player.currentScreenHandler.syncId, i, inventory.getSelectedSlot(), SlotActionType.SWAP, player);
            }
        }
        return player.isCreative() ? 64 : count;
    }
}
