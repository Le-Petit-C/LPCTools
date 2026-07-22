package lpctools.util.inGame;

import lpctools.mixinData.MixinData;
import lpctools.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class InGameUtils {
	public static void swapHandsHotkeyStyle(LocalPlayer player) {
		player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
		Inventory inv = player.getInventory();
		ItemStack offHandStack = inv.getItem(Inventory.SLOT_OFFHAND);
		inv.setItem(Inventory.SLOT_OFFHAND, inv.getSelectedItem());
		inv.setSelectedItem(offHandStack);
		DataUtils.clientMessage("Set main hand stack to " + offHandStack, true);
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
}
