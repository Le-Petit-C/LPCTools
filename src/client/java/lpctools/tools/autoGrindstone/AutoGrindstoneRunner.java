package lpctools.tools.autoGrindstone;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import lpctools.util.inGame.InGameManager;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

import static lpctools.tools.autoGrindstone.AutoGrindstone.*;

public class AutoGrindstoneRunner implements Registries.ContainerContentInitializedCallback, ToolUtils.ToolRunner {
	@Override public void onContainerContentInitialized(AbstractContainerMenu menu) {
		if(!(menu instanceof GrindstoneMenu)) return;
		InGameManager manager = InGameManager.get();
		if(manager == null) {
			AGConfig.setBooleanValue(false);
			return;
		}
		Inventory inventory = manager.getInventory();
		Object2IntOpenHashMap<String> enchantmentIds = new Object2IntOpenHashMap<>();
		for(String key : limitEnchantmentsConfig){
			String[] splits = key.split(";");
			if(splits.length >= 3){
				warnInvalidEnchantment(key);
				continue;
			}
			if(splits.length == 2){
				try{
					int maxLevel = Integer.parseInt(splits[1].trim());
					enchantmentIds.addTo(splits[0].trim(), maxLevel);
				}catch (NumberFormatException ignored){
					warnInvalidEnchantment(key);
				}
			}
			else enchantmentIds.addTo(splits[0].trim(), Integer.MAX_VALUE);
		}
		List<ItemStack> mainStacks = inventory.getNonEquipmentItems();
		for(int n = 0; n < mainStacks.size(); ++n){
			ItemStack stack = mainStacks.get(n);
			ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
			if(enchantments.isEmpty()) continue;
			boolean canErase = true;
			for(Object2IntMap.Entry<Holder<Enchantment>> enchantment : enchantments.entrySet()){
				String enchantmentId = enchantment.getKey().getRegisteredName();
				int enchantmentLevelLimit;
				if(enchantmentIds.containsKey(enchantmentId))
					enchantmentLevelLimit = enchantmentIds.getInt(enchantmentId);
				else {
					int lastIndex = enchantmentId.lastIndexOf(':');
					if (lastIndex == -1) return;
					String enchantmentIdTail = enchantmentId.substring(lastIndex + 1);
					if(!enchantmentIds.containsKey(enchantmentIdTail)){
						canErase = false;
						break;
					}
					enchantmentLevelLimit = enchantmentIds.getInt(enchantmentIdTail);
				}
				if(enchantmentLevelLimit < enchantment.getIntValue()){
					canErase = false;
					break;
				}
			}
			if(canErase){
				int slot = n < 9 ? n + 30 : n - 6;
				manager.handleContainerInput(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE);
				manager.handleContainerInput(menu.containerId, 2, 0, ContainerInput.THROW);
			}
		}
		manager.closeContainer();
	}
	private static void warnInvalidEnchantment(String key) {
		DataUtils.clientMessage(String.format("§eInvalid enchantment string: %s", key), false);
	}

	@Override public void registerAll(boolean b) {
		Registries.CLIENT_CONTAINER_CONTENT_INITIALIZED.register(this, b);
	}
}
