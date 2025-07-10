package lpctools.tools.liquidCleaner;

import lpctools.compact.derived.ShapeList;
import lpctools.util.GuiUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.NO_OPERATION;
import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.OPERATED;
import static lpctools.tools.liquidCleaner.LiquidCleaner.*;
import static lpctools.util.BlockUtils.*;

public class OnEndTick implements ClientTickEvents.EndTick {
    @Override public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            disableTool("nullClientPlayerEntity");
            return;
        }
        ClientWorld world = client.world;
        if (world == null) {
            disableTool("nullClientWorld");
            return;
        }
        ClientPlayerInteractionManager manager = client.interactionManager;
        if (manager == null) {
            disableTool("nullInteractionManager");
            return;
        }
        if (manager.getCurrentGameMode() == GameMode.SPECTATOR || manager.getCurrentGameMode() == GameMode.ADVENTURE){
            disableTool("unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getAsBoolean() && GuiUtils.isInTextOrGui()){
            disableTool("GUIOpened");
            return;
        }
        Iterable<BlockPos> iterateRegion = reachDistanceConfig.iterateFromClosest(player.getEyePos());
        ShapeList list = limitCleaningRange.buildShapeList();
        limitOperationSpeedConfig.resetOperationTimes();
        limitOperationSpeedConfig.iterableOperate(iterateRegion, pos -> {
            if (shouldAttackBlock(list, world, pos)) {
                manager.attackBlock(pos.toImmutable(), Direction.DOWN);
                return OPERATED;
            }
            return NO_OPERATION;
        });
        int offhandPriority = offhandFillingConfig.getAsBoolean() ? -1 : 0;
        Hand hand = offhandFillingConfig.getAsBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (HandRestock.search(this::isStackOk, offhandPriority) == -1) return;
        limitOperationSpeedConfig.iterableOperate(iterateRegion, pos -> {
            if (!list.testPos(pos)) {
                if (!expandRange.getAsBoolean()) return NO_OPERATION;
                boolean shouldContinue = true;
                for (Direction direction : Direction.values()) {
                    if (ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                    if (list.testPos(pos.offset(direction))) {
                        shouldContinue = false;
                        break;
                    }
                }
                if (shouldContinue) return NO_OPERATION;
            }
            BlockState state = world.getBlockState(pos);
            if (isReplaceableLiquid(state)) {
                limitOperationSpeedConfig.limitWithRestock(this::isStackOk, offhandPriority);
                Vec3d midPos = pos.toCenterPos();
                BlockHitResult hitResult = new BlockHitResult(midPos, Direction.DOWN, pos.mutableCopy(), false);
                manager.interactBlock(player, hand, hitResult);
                return OPERATED;
            }
            return NO_OPERATION;
        });
    }

    private static boolean shouldAttackBlock(@NotNull ShapeList shapeList ,@NotNull ClientWorld world, @NotNull BlockPos pos){
        BlockState state = world.getBlockState(pos);
        if(!shapeList.testPos(pos)){
            if(!expandRange.getAsBoolean()) return false;
            boolean isNear = false;
            for(Direction direction : Direction.values()){
                if(ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                if(shapeList.testPos(pos.offset(direction))){
                    isNear = true;
                    break;
                }
            }
            if(!isNear) return false;
            else if(!isContainingLiquid(state)) return false;
        }
        if(blockBlackListConfig.contains(state.getBlock())) return false;
        if(!isZeroHardBlock(state)) return false;
        if(state.isAir()) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(canAnyBucketPlaceAt(state)) return false;
        for(Direction direction : Direction.values()){
            if(ignoreDownwardTest.getAsBoolean() && direction == Direction.DOWN) continue;
            if(isContainingLiquid(world.getBlockState(pos.offset(direction)))) return false;
        }
        return true;
    }
    private boolean isStackOk(ItemStack stack){
        Item item = stack.getItem();
        if(item instanceof BlockItem blockItem && blockBlackListConfig.contains(blockItem.getBlock())) return false;
        if (!(item instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        if (block.getHardness() != 0) return false;
        BlockState state = block.getDefaultState();
        if (canAnyBucketPlaceAt(state)) return false;
        FluidState fluidState = state.getFluidState();
        if (fluidState.getLevel() != 0) return false;
        if (block instanceof Waterloggable) return false;
        //noinspection RedundantIfStatement
        if (block instanceof PlantBlock) return false;
        return true;
    }
}
