package lpctools.util.inGame;

import lpctools.mixin.client.accessors.LevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public record InGameManager(LocalPlayer player, MultiPlayerGameMode gameMode, ClientLevel level) {
	public static @Nullable InGameManager get(Minecraft mc) { return InGameUtils.getInGameGenericData(mc); }
	public static @Nullable InGameManager get() { return get(Minecraft.getInstance()); }

	public void closeContainer() { player.closeContainer(); }
	public @NotNull Inventory getInventory() { return player.getInventory(); }
	public @NotNull Vec3 playerPos() { return player.position(); }

	public @NotNull InteractionResult useItemOn(InteractionHand hand, BlockHitResult hitResult) { return gameMode.useItemOn(player, hand, hitResult); }
	public @NotNull InteractionResult useItemOn(InteractionHand hand, BlockPos pos) { return useItemOn(hand, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false)); }
	public @NotNull InteractionResult interact(Entity entity, EntityHitResult hitResult, InteractionHand hand) { return gameMode.interact(player, entity, hitResult, hand); }
	public @NotNull InteractionResult interact(Entity entity, InteractionHand hand) { return interact(entity, new EntityHitResult(entity), hand); }
	public void handleContainerInput(int containerId, int slotNum, int buttonNum, ContainerInput containerInput) { gameMode.handleContainerInput(containerId, slotNum, buttonNum, containerInput, player); }

	public @NotNull BlockState getBlockState(BlockPos pos) { return level.getBlockState(pos); }
	public @NotNull List<Entity> getEntities(@Nullable Entity except, AABB bb) { return level.getEntities(except, bb); }
	public @NotNull List<Entity> getEntities(AABB bb) { return getEntities(null, bb); }
	public @NotNull Iterable<Entity> getAllEntities() { return ((LevelAccessor)level).invokeGetEntities().getAll(); }

	public void send(final Packet<?> packet) { player.connection.send(packet); }

	public void swapHandsAutoStyle() { InGameUtils.swapHandsAutoStyle(player, gameMode); }

	public void selectMerchant(@NotNull MerchantMenu menu, int tradeIndex) {
		menu.setSelectionHint(tradeIndex);
		menu.tryMoveItems(tradeIndex);
		send(new ServerboundSelectTradePacket(tradeIndex));
	}
	public void setAnvilNameContent(@NotNull AnvilMenu menu, String newName) {
		if(menu.setItemName(newName)) send(new ServerboundRenameItemPacket(newName));
	}
}
