package lpctools.util;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class ItemUtils {
	public static int moveItems(AbstractContainerScreen<?> screen, int sourceSlotIndex, int targetSlotIndex, int maxCount, LocalPlayer player, MultiPlayerGameMode gameMode) {
		return moveItems(screen.getMenu(), sourceSlotIndex, targetSlotIndex, maxCount, player, gameMode);
	}
	// 在不超过给定上限的情况下尽可能尝试将一个slot的物品移动到另一个slot中，返回值为移动的物品数
	public static int moveItems(AbstractContainerMenu menu, int sourceSlotIndex, int targetSlotIndex, int maxCount, LocalPlayer player, MultiPlayerGameMode gameMode) {
		int movedCount = 0;
		ItemStack sourceStack = menu.slots.get(sourceSlotIndex).getItem();
		ItemStack targetStack = menu.slots.get(targetSlotIndex).getItem();
		if(!sourceStack.isEmpty() && (targetStack.isEmpty() || ItemStack.isSameItemSameComponents(sourceStack, targetStack))) {
			int initialSourceCount = sourceStack.getCount();
			int initialTargetRemaining = sourceStack.getMaxStackSize() - targetStack.getCount();
			if(initialSourceCount <= maxCount && initialSourceCount <= initialTargetRemaining) {
				// 可以直接合并stacks
				movedCount += sourceStack.getCount();
				gameMode.handleContainerInput(menu.containerId, sourceSlotIndex, 0, ContainerInput.PICKUP, player);
				gameMode.handleContainerInput(menu.containerId, targetSlotIndex, 0, ContainerInput.PICKUP, player);
			}
			else {
				// 需要保持source始终有至少一个物品，否则当容器接着漏斗时（或者类似的情况）有新物品进来没办法把剩下的物品放回去就糟糕了
				int holdingItemCount = 0;
				int sourceCount = initialSourceCount;
				int targetRemaining = initialTargetRemaining;
				while (movedCount < maxCount && targetRemaining > 0) {
					if(holdingItemCount <= 0) {
						// 右键拿取一半的物品
						gameMode.handleContainerInput(menu.containerId, sourceSlotIndex, 1, ContainerInput.PICKUP, player);
						int reservedCount = sourceCount >> 1;
						holdingItemCount = sourceCount - reservedCount;
						sourceCount = reservedCount;
					}
					int moveIfLeftClick = Math.min(targetRemaining, holdingItemCount);
					if(moveIfLeftClick + movedCount <= maxCount) {
						// 可以直接尝试将鼠标上的物品全部放到目标stack中
						gameMode.handleContainerInput(menu.containerId, targetSlotIndex, 0, ContainerInput.PICKUP, player);
						targetRemaining -= moveIfLeftClick;
						holdingItemCount -= moveIfLeftClick;
						movedCount += moveIfLeftClick;
					}
					else {
						// 超出允许放置的上限了，只能一个一个放
						// 但是先看看是“把物品一个一个放回原位再把剩下的全部放到目标位置”更好还是“把物品一个一个放到目标位置再把回剩下的全部放回原位”更好
						// 计算两种方案的“一个一个放”的操作次数，忽略最后一下“剩下的全放过去/回去”操作
						int putForwardFirstOperationCount = maxCount - movedCount;
						int putBackFirstOperationCount = holdingItemCount - (maxCount - movedCount);
						if(putForwardFirstOperationCount <= putBackFirstOperationCount) {
							gameMode.handleContainerInput(menu.containerId, targetSlotIndex, 1, ContainerInput.PICKUP, player);
							--targetRemaining;
							++movedCount;
						}
						else {
							gameMode.handleContainerInput(menu.containerId, sourceSlotIndex, 1, ContainerInput.PICKUP, player);
							++sourceCount;
						}
						--holdingItemCount;
					}
				}
				if(holdingItemCount > 0) {
					// 鼠标上还剩下一些物品，要把它们放回原位
					gameMode.handleContainerInput(menu.containerId, sourceSlotIndex, 0, ContainerInput.PICKUP, player);
					// sourceCount变量已经没有用了
					// sourceCount += holdingItemCount;
				}
			}
		}
		return movedCount;
	}
	public static int moveItems(AbstractContainerScreen<?> screen, int slotIndex, int maxCount, LocalPlayer player, MultiPlayerGameMode gameMode) {
		return moveItems(screen.getMenu(), slotIndex, maxCount, player, gameMode);
	}
	// 在不超过给定上限的情况下尽可能尝试将一个slot的物品从容器移动到背包或者从背包移动到容器，返回值为移动的物品数
	public static int moveItems(AbstractContainerMenu menu, int sourceSlotIndex, int maxCount, LocalPlayer player, MultiPlayerGameMode gameMode) {
		int movedCount = 0;
		try {
			// 优先匹配可堆叠在一起的相同物品
			for(int i = 0; movedCount < maxCount && i < menu.slots.size(); ++i) {
				Slot sourceSlot = menu.slots.get(sourceSlotIndex);
				Slot slot = menu.slots.get(i);
				if(slot.container != sourceSlot.container && ItemStack.isSameItemSameComponents(slot.getItem(), sourceSlot.getItem()))
					movedCount += moveItems(menu, sourceSlotIndex, i, maxCount - movedCount, player, gameMode);
			}
			// 其次匹配空stack，或者剩下所有可能的stack
			for(int i = 0; movedCount < maxCount && i < menu.slots.size(); ++i) {
				Slot sourceSlot = menu.slots.get(sourceSlotIndex);
				Slot slot = menu.slots.get(i);
				if(slot.container != sourceSlot.container && !slot.hasItem())
					movedCount += moveItems(menu, sourceSlotIndex, i, maxCount - movedCount, player, gameMode);
			}
		} catch (Exception e) {
			DataUtils.clientMessage(
				String.format(Component.translatable("lpctools.utils.warnExceptionMovingItem").getString(),
					menu.getClass().descriptorString(), e),
				false);
		}
		return movedCount;
	}
}
