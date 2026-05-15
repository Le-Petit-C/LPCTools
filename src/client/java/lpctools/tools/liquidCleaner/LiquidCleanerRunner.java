package lpctools.tools.liquidCleaner;

import lpctools.compact.derived.ShapeList;
import lpctools.util.GuiUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.LimitOperationSpeedConfig.OperationResult.NO_OPERATION;
import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.LimitOperationSpeedConfig.OperationResult.OPERATED;
import static lpctools.tools.liquidCleaner.LiquidCleaner.*;
import static lpctools.util.BlockUtils.*;

public class LiquidCleanerRunner implements ClientTickEvents.EndTick {
    @Override public void onEndTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            disableTool("nullClientPlayerEntity");
            return;
        }
        ClientLevel world = client.level;
        if (world == null) {
            disableTool("nullClientWorld");
            return;
        }
        MultiPlayerGameMode manager = client.gameMode;
        if (manager == null) {
            disableTool("nullInteractionManager");
            return;
        }
        if (manager.getPlayerMode() == GameType.SPECTATOR || manager.getPlayerMode() == GameType.ADVENTURE){
            disableTool("unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getAsBoolean() && GuiUtils.isInTextOrGui()){
            disableTool("GUIOpened");
            return;
        }
        Iterable<BlockPos> iterateRegion = reachDistanceConfig.iterateFromClosest(player.getEyePosition());
        ShapeList list = limitCleaningRange.buildShapeList();
        limitOperationSpeedConfig.resetOperationTimes();
        limitOperationSpeedConfig.iterableOperate(iterateRegion, pos -> {
            if (shouldAttackBlock(list, player, pos)) {
                manager.startDestroyBlock(pos.immutable(), Direction.DOWN);
                return OPERATED;
            }
            return NO_OPERATION;
        });
        int offhandPriority = offhandFillingConfig.getAsBoolean() ? -1 : 0;
        InteractionHand hand = offhandFillingConfig.getAsBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (HandRestock.search(this::isStackOk, offhandPriority) == -1) return;
        limitOperationSpeedConfig.iterableOperate(iterateRegion, pos -> {
            if (!list.testPos(pos)) {
                if (!expandRange.getAsBoolean()) return NO_OPERATION;
                boolean shouldContinue = true;
                for (Direction direction : Direction.values()) {
                    if (ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                    if (list.testPos(pos.relative(direction))) {
                        shouldContinue = false;
                        break;
                    }
                }
                if (shouldContinue) return NO_OPERATION;
            }
            BlockState state = world.getBlockState(pos);
            if (isAllowedReplaceableLiquid(state)) {
                limitOperationSpeedConfig.limitWithRestock(this::isStackOk, offhandPriority);
                Vec3 midPos = pos.getCenter();
                BlockHitResult hitResult = new BlockHitResult(midPos, Direction.DOWN, pos.mutable(), false);
                manager.useItemOn(player, hand, hitResult);
                return OPERATED;
            }
            return NO_OPERATION;
        });
    }
    
    private static boolean isAllowedReplaceableLiquid(BlockState state) {
        if(!isReplaceableLiquid(state)) return false;
		return !liquidSourceOnly.getAsBoolean() || state.getFluidState().isSource();
	}

    private static boolean shouldAttackBlock(@NotNull ShapeList shapeList, @NotNull LocalPlayer player, @NotNull BlockPos pos){
        Level world = player.level();
        BlockState state = world.getBlockState(pos);
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
        if(!canBreakInstantly(player, pos)) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(!cleaningBlocks.contains(state.getBlock())) return false;
        for(Direction direction : Direction.values()){
            if(ignoreDownwardTest.getAsBoolean() && direction == Direction.DOWN) continue;
            if(isContainingLiquid(world.getBlockState(pos.relative(direction)))) return false;
        }
        return true;
    }
    private boolean isStackOk(ItemStack stack){
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return false;
        return cleaningBlocks.contains(blockItem.getBlock());
    }
}
