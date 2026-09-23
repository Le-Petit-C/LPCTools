package lpctools.util.inGame;

import lpctools.mixinData.MixinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class InGameUtils {
	public static void swapHandsHotkeyStyle(LocalPlayer player) {
		player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
		Inventory inv = player.getInventory();
		ItemStack offHandStack = inv.getItem(Inventory.SLOT_OFFHAND);
		inv.setItem(Inventory.SLOT_OFFHAND, inv.getSelectedItem());
		inv.setSelectedItem(offHandStack);
	}
	public static void swapHandsMenuStyle(LocalPlayer player, MultiPlayerGameMode gameMode) {
		gameMode.handleContainerInput(player.containerMenu.containerId,
			MixinData.getData(player.containerMenu).getHotbarStartIndexOrDefault() + player.getInventory().getSelectedSlot(),
			Inventory.SLOT_OFFHAND, ContainerInput.SWAP, player);
	}
	public static void swapHandsAutoStyle(LocalPlayer player, MultiPlayerGameMode gameMode) {
		if(Minecraft.getInstance().gui.screen() != null) swapHandsMenuStyle(player, gameMode);
		else swapHandsHotkeyStyle(player);
	}
	public static @Nullable InGameManager getInGameGenericData(Minecraft mc) {
		LocalPlayer player = mc.player;
		MultiPlayerGameMode gameMode = mc.gameMode;
		ClientLevel level = mc.level;
		if(player == null || gameMode == null || level == null) return null;
		else return new InGameManager(player, gameMode, level);
	}
	public static <T> @Nullable Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null) return null;
		return player.connection.registryAccess().lookup(key).orElse(null);
	}
}
