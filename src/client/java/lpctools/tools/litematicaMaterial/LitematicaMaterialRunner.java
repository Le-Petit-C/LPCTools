package lpctools.tools.litematicaMaterial;

import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import lpctools.compact.litematica.LitematicaMethods;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.DataUtils;
import lpctools.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lpctools.tools.litematicaMaterial.LitematicaMaterial.*;

class LitematicaMaterialRunner implements ILPCValueChangeCallback, Registries.ScreenChangeCallback, Runnable {
	private final LitematicaMethods litematicaMethods;
	private AbstractContainerScreen<?> containerScreen;
	LitematicaMaterialRunner(LitematicaMethods litematicaMethods) {
		this.litematicaMethods = litematicaMethods;
	}

	@Override public void onValueChanged() { Registries.ON_SCREEN_CHANGED.register(this, LMConfig.getBooleanValue()); }

	@Override public void run() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		MultiPlayerGameMode gameMode = mc.gameMode;
		MaterialListBase materialListBase = litematicaMethods.getMaterialList();
		if(mc.screen == containerScreen && player != null && gameMode != null && materialListBase != null) {
			MaterialListUtils.updateAvailableCounts(materialListBase.getMaterialsAll(), player);
			HashMap<Item, ArrayList<ItemLackingData>> lackItems = new HashMap<>();
			for(MaterialListEntry entry : materialListBase.getMaterialsAll()) {
				int lackingCount = entry.getCountMissing() - entry.getCountAvailable();
				if(lackingCount > 0) {
					lackItems.computeIfAbsent(entry.getStack().getItem(), _ ->new ArrayList<>())
						.add(new ItemLackingData(entry.getStack(), lackingCount));
				}
			}
			AbstractContainerMenu containerMenu = containerScreen.getMenu();
			List<Slot> menuSlots = containerMenu.slots;
			int movedItemCount = 0;
			boolean warnNotEnoughSpace = false;
			for(int i = 0; i < menuSlots.size(); ++i) {
				Slot slot = menuSlots.get(i);
				if(!(slot.container instanceof Inventory)) {
					// slot不是背包的槽位时才尝试进行移动，我们要做的是收集材料到背包
					ItemStack stack = slot.getItem();
					Item stackItem = stack.getItem();
					if(lackItems.get(stackItem) instanceof ArrayList<ItemLackingData> list) {
						for(int j = 0; j < list.size(); ++j) {
							ItemLackingData lackingItem = list.get(j);
							if(ItemStack.isSameItemSameComponents(stack, lackingItem.itemStack)) {
								// 物品匹配，可以收集对应物品
								int expectedMovedCount = Math.min(lackingItem.lackingCount, stack.getCount());
								int movedItemCountInReality = ItemUtils.moveItems(containerMenu, i, lackingItem.lackingCount, player, gameMode);
								if(expectedMovedCount != movedItemCountInReality) warnNotEnoughSpace = true;
								lackingItem.lackingCount -= movedItemCountInReality;
								movedItemCount += movedItemCountInReality;
								if(lackingItem.lackingCount <= 0) {
									// 已经拿到足够的物品了
									list.set(j, list.getLast());
									list.removeLast();
									if(list.isEmpty()) lackItems.remove(stackItem);
								}
								// 假设投影mod内的材料列表不会重复列出两个sameItemSameComponents的物品，那么此处可以直接退出
								break;
							}
						}
					}
				}
			}
			if(warnNotEnoughSpace)
				DataUtils.clientMessage(
					String.format("[%s] %s", LMConfig.getNameTranslation(),
						Component.translatable("lpctools.tools.LM.warnNotEnoughSpace").getString()),
					false);
			DataUtils.clientMessage(
				String.format("[%s] " + Component.translatable("lpctools.tools.LM.movedInfoText").getString(), LMConfig.getNameTranslation(),
					movedItemCount),
				true);
			MaterialListUtils.updateAvailableCounts(materialListBase.getMaterialsAll(), player);
			player.closeContainer();
		}
		containerScreen = null;
	}

	private static class ItemLackingData {
		ItemStack itemStack;
		int lackingCount;
		ItemLackingData(ItemStack itemStack, int lackingCount) {
			this.itemStack = itemStack;
			this.lackingCount = lackingCount;
		}
	}

	@Override public void onScreenChanged(Screen newScreen) {
		if(newScreen instanceof AbstractContainerScreen<?> _containerScreen
			&& !(_containerScreen instanceof InventoryScreen) && !(_containerScreen instanceof CreativeModeInventoryScreen)
			&& (warehouseContainers.get().contains(_containerScreen.getTitle().getString())
			|| (buildingMode.getBooleanValue() && materialContainers.get().contains(_containerScreen.getTitle().getString())))) {
			if(this.containerScreen == null) Minecraft.getInstance().schedule(this);
			this.containerScreen = _containerScreen;
		}
	}
}
