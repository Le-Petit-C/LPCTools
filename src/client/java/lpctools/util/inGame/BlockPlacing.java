package lpctools.util.inGame;

import lpctools.generic.Bypassing;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.DataUtils;
import lpctools.util.DirectionVectorPredicator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockPlacing extends InGameOperationRunner.BasicOperation<BlockPlacing, BlockPlacing.PlacingState, BlockPlacing.BlockPlacingRunner> implements BlockInteraction.Prepare, Consumer<BlockInteraction> {
	private final BlockPos pos;
	private final Predicate<ItemStack> restockTest;
	private final Predicate<BlockState> blockTest;
	private final InteractionHand preferredHand;
	private final boolean forcePreferredHand;
	private final @Nullable BlockInteraction[] interactions = new BlockInteraction[7];
	private final @Nullable Direction requiredInteractDirection;
	private final @Nullable DirectionVectorPredicator requiredPlayerDirection;

	private BlockPlacing(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest,
	                     InteractionHand preferredHand, boolean forcePreferredHand,
	                     @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		super(PlacingState.WAITING);
		this.pos = pos.immutable();
		this.restockTest = restockTest;
		this.blockTest = blockTest;
		this.preferredHand = preferredHand;
		this.forcePreferredHand = forcePreferredHand;
		this.requiredInteractDirection = requiredInteractDirection;
		this.requiredPlayerDirection = requiredPlayerDirection;
		scheduleUpdate();
	}

	public static BlockPlacing schedulePlace(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest,
											 InteractionHand preferredHand, boolean forcePreferredHand,
											 @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return new BlockPlacing(pos, restockTest, blockTest, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection);
	}

	public static BlockPlacing schedulePlace(BlockPos pos, Block targetBlock, InteractionHand preferredHand, boolean forcePreferredHand,
											 @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return schedulePlace(pos, stack -> stack.getItem() == targetBlock.asItem(), state -> state.is(targetBlock), preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection);
	}

	public static BlockPlacing schedulePlace(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest, InteractionHand hand) {
		return schedulePlace(pos, restockTest, blockTest, hand, true, null, null);
	}

	public static BlockPlacing schedulePlace(BlockPos pos, Block targetBlock, InteractionHand preferredHand) {
		return schedulePlace(pos, targetBlock, preferredHand, true, null, null);
	}

	@Override @NonNull BlockPlacingRunner getRunner() { return runner; }
	@Override public BlockPlacing getThis() { return this; }

	@Override public @Nullable InteractionHand prepare(BlockInteraction instance, OperationSpeedLimit limit) {
		// TODO: 潜行检测/潜行计划
		InGameManager manager = InGameManager.get();
		if(manager == null) return null;
		if(requiredPlayerDirection != null && !requiredPlayerDirection.test(manager.playerViewVector())) return null;
		InteractionHand res;
		if(restockTest.test(manager.getItemInHand(preferredHand)))
			res = preferredHand;
		else if(!forcePreferredHand && restockTest.test(manager.getItemInHand(DataUtils.oppositeHand(preferredHand))))
			res = DataUtils.oppositeHand(preferredHand);
		else {
			var restockLimit = limit.limitWithRestock(restockTest, preferredHand);
			if(restockLimit.hasReservedTimes()) {
				res = preferredHand;
				restockLimit.applyRestock();
			}
			else if (forcePreferredHand) res = null;
			else {
				var anotherRestockLimit = limit.limitWithRestock(restockTest, DataUtils.oppositeHand(preferredHand));
				if(anotherRestockLimit.hasReservedTimes()) {
					res = DataUtils.oppositeHand(preferredHand);
					anotherRestockLimit.applyRestock();
				}
				else res = null;
			}
		}
		return res;
	}

	@Override public void cancel() {
		super.cancel();
		for(var interaction : interactions)
			if(interaction != null) interaction.cancel();
	}

	@Override public PlacingState getCancelState() { return PlacingState.CANCELED; }

	@Override public void accept(BlockInteraction blockInteraction) {
		if (blockInteraction.getState().isResultState) {
			int index = directionOrdinal(directionOpposite(blockInteraction.targetInteractDirection));
			// 检查一下以防万一
			if (interactions[index] == blockInteraction) interactions[index] = null;
		}
	}

	@Override public BlockPos getPos() { return pos; }

	public enum PlacingState implements ResultMarkedState {
		SUCCEEDED(true, true),
		WAITING(false, false),
		CANCELED(true, false);
		public final boolean isResultState, succeeded;
		PlacingState(boolean isResultState, boolean succeeded) {
			this.isResultState = isResultState;
			this.succeeded = succeeded;
		}
		@Override public boolean isResultState() { return isResultState; }
		@Override public boolean succeeded() { return succeeded; }
	}

	private void processSucceeded() {
		for(var interaction : interactions)
			if(interaction != null) interaction.cancel();
		setState(PlacingState.SUCCEEDED);
	}

	private void updatePos() {
		for(var interaction : interactions)
			if(interaction != null) interaction.cancel();
		Arrays.fill(interactions, null);
		if(getState().isResultState) return;
		InGameManager manager = InGameManager.get();
		if(manager == null) return;
		if(blockTest.test(manager.getBlockState(pos))) {
			processSucceeded();
			return;
		}
		if(!manager.getBlockState(pos).canBeReplaced()) return;
		BlockPlaceBypassMethod method = Bypassing.bypassing.getBooleanValue() ? Bypassing.blockPlaceBypass.get() : BlockPlaceBypassMethod.NONE;
		if(!method.isValidPos(manager, pos)) return;
		for(var direction : Direction.values()) {
			if(requiredInteractDirection != null && direction != requiredInteractDirection) continue;
			var relativePos = pos.relative(direction.getOpposite());
			if(!manager.getBlockState(relativePos).canBeReplaced())
				interactions[direction.ordinal()] = BlockInteraction.scheduleInteract(relativePos, direction, this);
		}
		if(method != BlockPlaceBypassMethod.ATTACH || !manager.getBlockState(pos).isAir())
			interactions[6] = BlockInteraction.scheduleInteract(pos, requiredInteractDirection, this);
		for(var interaction : interactions)
			if(interaction != null) interaction.setCallback(this);
	}

	private static final BlockPlacingRunner runner = new BlockPlacingRunner();

	// blockPlace的targetDirection为放在哪个方向的方块上，玩家朝向需求由BlockPlacing处理
	public interface StatusCalculator {
		boolean isValidPos(InGameManager manager, BlockPos targetPos);
	}

	static class BlockPlacingRunner extends BlockOperationRunner<BlockPlacing, StatusCalculator, BlockPlaceBypassMethod> implements Registries.ClientWorldChunkSetBlockState {
		BlockPlacingRunner() { super(Bypassing.blockPlaceBypass); }

		@Override protected void updateCached(BlockPlacing instance) {
			super.updateCached(instance);
			instance.updatePos();
		}

		@Override protected boolean prepare(TickOperationBasicData<StatusCalculator> data) { return false; }
		@Override protected boolean forEachPrepare(TickOperationBasicData<StatusCalculator> data) { return false; }
		@Override protected boolean forEachAction(TickOperationBasicData<StatusCalculator> data, BlockPos pos, Collection<BlockPlacing> operationInstances) { return false; }
		@Override protected void afterAction(TickOperationBasicData<StatusCalculator> data) {}
		@Override void registerAllRaw(boolean b) { Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b); super.registerAllRaw(b); }

		@Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
			updatePos(pos, null);
			for(var direction : Direction.values())
				updatePos(pos, direction);
		}

		private void updatePos(BlockPos pos, @Nullable Direction direction) {
			Collection<BlockPlacing> instances = getOperations(directionRelative(pos, directionOpposite(direction)));
			if(instances == null) return;
			for(BlockPlacing placing : instances) placing.updatePos();
		}
	}

	static int directionOrdinal(@Nullable Direction direction) { return direction == null ? 6 : direction.ordinal(); }
	static Direction directionOpposite(@Nullable Direction direction) { return direction == null ? null : direction.getOpposite(); }
	static BlockPos directionRelative(BlockPos pos, Direction direction) { return direction == null ? pos : pos.relative(direction); }
}
