package lpctools.tools.liquidCleaner;

import lpctools.compact.derived.ShapeList;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.GuiUtils;
import lpctools.util.inGame.BlockBreaking;
import lpctools.util.inGame.InGameManager;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.liquidCleaner.LiquidCleaner.*;
import static lpctools.util.BlockUtils.*;

class LiquidCleanerRunner implements ClientTickEvents.EndTick, ToolUtils.ToolRunner, QuietAutoCloseable {
    final BlockBreaking.BlockBreakingCollection breakingCollection = BlockBreaking.createBreakingCollection();
    @Override public void onEndTick(@NonNull Minecraft client) {
        if(client.isPaused()) return;
        InGameManager manager = InGameManager.get(client);
        if (manager == null) {
            disableTool("notInGame");
            return;
        }
        if (manager.gameType() == GameType.SPECTATOR || manager.gameType() == GameType.ADVENTURE){
            disableTool("unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getAsBoolean() && GuiUtils.isInTextOrGui()){
            disableTool("GUIOpened");
            return;
        }
        Iterable<BlockPos> iterateRegion = reachDistanceConfig.iterateFromClosest(manager.playerEyePos());
        ShapeList list = limitCleaningRange.buildShapeList();
        try (var scheduler = breakingCollection.startUpdateBreakings()) {
            for(BlockPos pos : iterateRegion)
                if (shouldBreakBlock(manager, list, pos))
                    scheduler.scheduleBreak(pos);
        }
        var restockedLimit = OperationSpeedLimit.root().limitWithRestock(this::isStackOk, interactionHand.offhandPriority());
        for(BlockPos pos : iterateRegion) {
            if(!restockedLimit.hasReservedTimes()) break;
            if (!list.testPos(pos)) {
                if (!expandRange.getAsBoolean()) continue;
                boolean shouldContinue = true;
                for (Direction direction : Direction.values()) {
                    if (ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                    if (list.testPos(pos.relative(direction))) {
                        shouldContinue = false;
                        break;
                    }
                }
                if (shouldContinue) continue;
            }
            BlockState state = manager.getBlockState(pos);
            if (isAllowedReplaceableLiquid(state)) {
                restockedLimit.costInteractBlock();
                manager.useItemOn(interactionHand.getHand(), pos);
            }
        }
    }
    
    private static boolean isAllowedReplaceableLiquid(BlockState state) {
        if(!isReplaceableLiquid(state)) return false;
		return !liquidSourceOnly.getAsBoolean() || state.getFluidState().isSource();
	}

    private static boolean shouldBreakBlock(InGameManager manager, ShapeList shapeList, BlockPos pos){
        BlockState state = manager.getBlockState(pos);
        if(!shapeList.testPos(pos)){
            if(!expandRange.getAsBoolean()) return false;
            boolean isNear = false;
            for(Direction direction : Direction.values()){
                if(ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                if(shapeList.testPos(pos.relative(direction))){
                    isNear = true;
                    break;
                }
            }
            if(!isNear) return false;
            else if(!isContainingLiquid(state)) return false;
        }
        // if(!canBreakInstantly(manager.player, pos)) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(!cleaningBlocks.contains(state.getBlock())) return false;
        for(Direction direction : Direction.values()){
            if(ignoreDownwardTest.getAsBoolean() && direction == Direction.DOWN) continue;
            if(isContainingLiquid(manager.getBlockState(pos.relative(direction)))) return false;
        }
        return true;
    }
    private boolean isStackOk(ItemStack stack){
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return false;
        return cleaningBlocks.contains(blockItem.getBlock());
    }

    @Override public void close() {
        registerAll(false);
        breakingCollection.close();
    }

    @Override public void registerAll(boolean b) { Registries.END_CLIENT_TICK.register(this, b); }
}
