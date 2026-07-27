package lpctools.util.inGame;

import lpctools.generic.GenericUtils;
import lpctools.mixin.client.accessors.LevelAccessor;
import lpctools.mixin.client.accessors.MultiPlayerGameModeAccessor;
import lpctools.mixinData.MixinData;
import lpctools.mixinData.MultiPlayerGameModeExtraData;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class InGameManager {
	public final LocalPlayer player;
	public final MultiPlayerGameMode gameMode;
	public final ClientLevel level;
	private GenericUtils.MobSpawnTest spawnTest = null;

	public InGameManager(LocalPlayer player, MultiPlayerGameMode gameMode, ClientLevel level) {
		this.player = player;
		this.gameMode = gameMode;
		this.level = level;
	}

	public static @Nullable InGameManager get(Minecraft mc) { return InGameUtils.getInGameGenericData(mc); }
	public static @Nullable InGameManager get() { return get(Minecraft.getInstance()); }

	public void closeContainer() { player.closeContainer(); }
	public @NotNull Inventory getInventory() { return player.getInventory(); }
	public @NotNull Vec3 playerPos() { return player.position(); }
	public @NotNull Vec3 playerEyePos() { return player.getEyePosition(); }
	public boolean isShiftKeyDown() { return player.isShiftKeyDown(); }
	public GameType gameType() { return player.gameMode(); }
	public Direction playerDirection() { return player.getDirection(); }
	public double blockInteractionRange() { return player.blockInteractionRange(); }
	public double entityInteractionRange() { return player.entityInteractionRange(); }
	public void swing(InteractionHand hand) { player.swing(hand); }

	public MultiPlayerGameModeExtraData gameModeExtraData() { return MixinData.getData(gameMode); }
	public @NotNull InteractionResult useItemOn(InteractionHand hand, BlockHitResult hitResult) { return gameMode.useItemOn(player, hand, hitResult); }
	public @NotNull InteractionResult useItemOn(InteractionHand hand, BlockPos pos) { return useItemOn(hand, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos.immutable(), false)); }
	public @NotNull InteractionResult interact(Entity entity, EntityHitResult hitResult, InteractionHand hand) { return gameMode.interact(player, entity, hitResult, hand); }
	public @NotNull InteractionResult interact(Entity entity, InteractionHand hand) { return interact(entity, new EntityHitResult(entity), hand); }
	public void handleContainerInput(int containerId, int slotNum, int buttonNum, ContainerInput containerInput) { gameMode.handleContainerInput(containerId, slotNum, buttonNum, containerInput, player); }
	public boolean startDestroyBlock(BlockPos pos, Direction direction) { return gameMode.startDestroyBlock(pos, direction); }
	public boolean continueDestroyBlock(BlockPos pos, Direction direction) { return gameMode.continueDestroyBlock(pos, direction); }
	public void stopDestroyBlock() { gameMode.stopDestroyBlock(); }
	public boolean isDestroying() { return gameMode.isDestroying(); }
	public BlockPos getDestroyBlockPos() { return ((MultiPlayerGameModeAccessor)gameMode).getDestroyBlockPos(); }
	public int getDestroyDelay() { return ((MultiPlayerGameModeAccessor)gameMode).getDestroyDelay(); }

	public DimensionType dimensionType() { return level.dimensionType(); }
	public @NotNull BlockState getBlockState(BlockPos pos) { return level.getBlockState(pos); }
	public @NotNull List<Entity> getEntities(@Nullable Entity except, AABB bb) { return level.getEntities(except, bb); }
	public @NotNull List<Entity> getEntities(AABB bb) { return getEntities(null, bb); }
	public @NotNull Iterable<Entity> getAllEntities() { return ((LevelAccessor)level).invokeGetEntities().getAll(); }
	public boolean mayMobSpawnAt(BlockPos pos) { if(spawnTest == null) spawnTest = GenericUtils.createSpawnTest(); return spawnTest.mayMobSpawnAt(level, level.getLightEngine(), pos); }
	public float getDestroyProgress(BlockPos pos) { return getBlockState(pos).getDestroyProgress(player, level, pos); }
	public void addBreakingBlockEffect(BlockPos pos, Direction direction) { level.addBreakingBlockEffect(pos, direction); }

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
