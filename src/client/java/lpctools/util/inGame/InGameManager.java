package lpctools.util.inGame;

import lpctools.generic.GenericUtils;
import lpctools.mixin.client.accessors.LevelAccessor;
import lpctools.mixin.client.accessors.LocalPlayerAccessor;
import lpctools.mixin.client.accessors.MultiPlayerGameModeAccessor;
import lpctools.mixinData.MixinData;
import lpctools.mixinData.MultiPlayerGameModeExtraData;
import lpctools.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
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
	public static @NonNull InGameManager getOrThrow(Minecraft mc) {
		if(get(mc) instanceof InGameManager manager) return manager;
		// TODO notInGame translatable component
		else throw new RuntimeException();
	}
	public static @NonNull InGameManager getOrThrow() { return getOrThrow(Minecraft.getInstance()); }

	public void closeContainer() { player.closeContainer(); }
	public @NotNull Inventory getInventory() { return player.getInventory(); }
	public @NotNull Vec3 playerPos() { return player.position(); }
	public @NotNull Vec3 playerEyePos() { return player.getEyePosition(); }
	public @NotNull Vec3 playerViewVector() { return player.getViewVector(1); }
	public boolean isShiftKeyDown() { return player.isShiftKeyDown(); }
	public GameType gameType() { return player.gameMode(); }
	public Direction playerDirection() { return player.getDirection(); }
	public Direction playerNearstViewDirection() { return player.getNearestViewDirection(); }
	public double blockInteractionRange() { return player.blockInteractionRange(); }
	public double entityInteractionRange() { return player.entityInteractionRange(); }
	public void swing(InteractionHand hand) { player.swing(hand); }
	public HitResult raycastHitResult() { return player.raycastHitResult(1, player); }
	public float getYRotRaw() { return player.getYRot(); }
	public float getXRotRaw() { return player.getXRot(); }
	public float getYRot() { return getYRotRaw() * (Mth.PI / 180); }
	public float getXRot() { return getXRotRaw() * (Mth.PI / 180); }
	public void setRotRaw(float YRot, float XRot) { player.setYRot(YRot); player.setXRot(XRot); }
	public void setRot(float YRot, float XRot) { player.setYRot(YRot * (180 / Mth.PI)); player.setXRot(XRot * (180 / Mth.PI)); }
	public void setRot(Vector3fc targetView, float defYRot, float defXRot) { setRot(
		MathUtils.YRotOrDefault(targetView.x(), targetView.y(), targetView.z(), defYRot),
		MathUtils.XRotOrDefault(targetView.x(), targetView.y(), targetView.z(), defXRot));
	}
	public float yRotLastRaw() { return ((LocalPlayerAccessor)player).getYRotLast(); }
	public float xRotLastRaw() { return ((LocalPlayerAccessor)player).getXRotLast(); }
	public float yRotLast() { return yRotLastRaw() * (Mth.PI / 180); }
	public float xRotLast() { return xRotLastRaw() * (Mth.PI / 180); }
	public ItemStack getMainHandItem() { return player.getMainHandItem(); }
	public ItemStack getOffHandItem() { return player.getOffhandItem(); }
	public ItemStack getItemInHand(InteractionHand hand) { return player.getItemInHand(hand); }

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
	public @NotNull Block getBlock(BlockPos pos) { return level.getBlockState(pos).getBlock(); }
	public @NotNull List<Entity> getEntities(@Nullable Entity except, AABB bb) { return level.getEntities(except, bb); }
	public @NotNull List<Entity> getEntities(AABB bb) { return getEntities(null, bb); }
	public @NotNull Iterable<Entity> getAllEntities() { return ((LevelAccessor)level).invokeGetEntities().getAll(); }
	public boolean mayMobSpawnAt(BlockPos pos) { if(spawnTest == null) spawnTest = GenericUtils.createSpawnTest(); return spawnTest.mayMobSpawnAt(level, level.getLightEngine(), pos); }
	public float getDestroyProgress(BlockPos pos) { return getBlockState(pos).getDestroyProgress(player, level, pos); }
	public void addBreakingBlockEffect(BlockPos pos, Direction direction) { level.addBreakingBlockEffect(pos, direction); }
	public VoxelShape getBlockShape(BlockPos pos) { return level.getBlockState(pos).getShape(level, pos); }
	public VoxelShape getMovedBlockShape(BlockPos pos) { return getBlockShape(pos).move(pos); }
	public VoxelShape getBlockInteractionShape(BlockPos pos) { return level.getBlockState(pos).getInteractionShape(level, pos); }
	public VoxelShape getMovedBlockInteractionShape(BlockPos pos) { return getBlockInteractionShape(pos).move(pos); }
	public VoxelShape getBlockCollisionShape(BlockPos pos) { return level.getBlockState(pos).getCollisionShape(level, pos); }
	public VoxelShape getMovedBlockCollisionShape(BlockPos pos) { return getBlockCollisionShape(pos).move(pos); }

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
