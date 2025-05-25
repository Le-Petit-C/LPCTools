package lpctools.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HandRestock {
    public interface IRestockTest{
        boolean isStackOk(ItemStack stack);
    }
    public record SearchInSet(@NotNull Set<Item> set) implements IRestockTest{
        @Override public boolean isStackOk(ItemStack stack){
            return set.contains(stack.getItem());
        }
    }
    //从背包里寻找第一个满足条件的物品槽索引，主手槽位先于一般槽检测。没找到则返回-1，找到副手返回40
    //offhandPriority:副手槽位的检测优先级，-1表示在主手之前，0表示在主手之后但是在其他槽位之前，1表示在所有槽位之后，其他表示不检测
    public static int search(IRestockTest restockTest, int offhandPriority){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return -1;
        PlayerInventory inventory = player.getInventory();
        if(offhandPriority == -1 && restockTest.isStackOk(ItemUtils.getOffhandStack(inventory))) return 40;
        if(restockTest.isStackOk(inventory.getSelectedStack())) return inventory.getSelectedSlot();
        if(offhandPriority == 0 && restockTest.isStackOk(ItemUtils.getOffhandStack(inventory))) return 40;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(restockTest.isStackOk(stack)) return i;
        }
        if(offhandPriority == 1 && restockTest.isStackOk(ItemUtils.getOffhandStack(inventory))) return 40;
        return -1;
    }
    //从背包里寻找集合中的物品并将其换到手上，成功使主手拿上给定物品返回拿到的物品数量，失败返回0
    //offhandPriority与search中的offhandPriority同义，进一步地，如果offhandPriority=-1即副手优先级比主手高，则变成填充副手而不是主手
    //restockOffhand表示是否填充副手
    public static int restock(IRestockTest restockTest, int offhandPriority){
        int i = search(restockTest, offhandPriority);
        if(i == -1) return 0;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;
        PlayerInventory inventory = player.getInventory();
        if(inventory == null) return 0;
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(itm == null) return 0;
        int count = inventory.getStack(i).getCount();
        if(offhandPriority == -1){
            if(i != 40)
                ItemUtils.swapSlotWithOffhand(i);
        }
        else{
            if(i < 9) inventory.setSelectedSlot(i);
            else ItemUtils.swapSlotWithMainhand(i);
        }
        return count;
    }
}
