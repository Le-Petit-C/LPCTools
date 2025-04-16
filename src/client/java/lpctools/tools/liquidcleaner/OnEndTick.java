package lpctools.tools.liquidcleaner;

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
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static lpctools.tools.liquidcleaner.LiquidCleaner.*;
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
        double d = reachDistanceConfig.getAsDouble();
        if(limitInteractSpeedConfig.getAsBoolean()){
            if(canInteractBlockCount > 1) canInteractBlockCount = 0;
            canInteractBlockCount += maxBlockPerTickConfig.getAsDouble();
        }
        else canInteractBlockCount = Double.MAX_VALUE;
        Box iterateBox = getIterateBox(client, d);
        Iterable<BlockPos> iterateRegion = BlockPos.iterate(
                BlockPos.ofFloored(iterateBox.getMinPos()), BlockPos.ofFloored(iterateBox.getMaxPos()));
        ShapeList list = limitCleaningRange.buildShapeList();
        for(BlockPos pos1 : iterateRegion){
            BlockPos pos = new BlockPos(pos1);//固定当前BlockPos
            Vec3d midPos = pos.toCenterPos();
            if (midPos.subtract(player.getEyePos()).length() >= d) continue;
            if (shouldAttackBlock(list, world, pos)){
                manager.attackBlock(pos, Direction.DOWN);
                if(--canInteractBlockCount < 1) return;
            }
        }
        int offhandPriority = offhandFillingConfig.getAsBoolean() ? -1 : 0;
        Hand hand = offhandFillingConfig.getAsBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (HandRestock.search(this::isStackOk, offhandPriority) == -1) return;
        for(BlockPos pos : iterateRegion){
            if(!list.testPos(pos)){
                if(!expandRange.getAsBoolean()) continue;
                boolean shouldContinue = true;
                for(Direction direction : Direction.values()){
                    if(ignoreDownwardTest.getAsBoolean() && direction == Direction.UP) continue;
                    if(list.testPos(pos.offset(direction))){
                        shouldContinue = false;
                        break;
                    }
                }
                if(shouldContinue) continue;
            }
            Vec3d midPos = pos.toCenterPos();
            if (midPos.subtract(player.getEyePos()).length() >= d) continue;
            BlockState state = world.getBlockState(pos);
            if (isReplaceableLiquid(state)) {
                if (!HandRestock.restock(this::isStackOk, offhandPriority)) return;
                BlockHitResult hitResult = new BlockHitResult(midPos, Direction.DOWN, pos.mutableCopy(), false);
                manager.interactBlock(player, hand, hitResult);
                if (--canInteractBlockCount < 1) return;
            }
        }
    }

    private double canInteractBlockCount = 0;
    private Box getIterateBox(@NotNull MinecraftClient client, double reachDistance){
        if(client.player == null || client.world == null)
            return new Box(0, 0, 0, 0, 0, 0);
        Vec3d eyePos = client.player.getEyePos();
        double minX = eyePos.getX() - reachDistance;
        double maxX = eyePos.getX() + reachDistance;
        double minY = eyePos.getY() - reachDistance;
        double maxY = eyePos.getY() + reachDistance;
        double minZ = eyePos.getZ() - reachDistance;
        double maxZ = eyePos.getZ() + reachDistance;
        DimensionType dimensionType = client.world.getDimension();
        if(minY < dimensionType.minY()) minY = dimensionType.minY();
        double dimensionTop = dimensionType.minY() + dimensionType.height();
        if(maxY > dimensionTop) maxY = dimensionTop;
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
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
        if(blacklistBlocks.contains(state.getBlock())) return false;
        if(!isZeroHardBlock(state)) return false;
        if(state.isAir()) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(canBeReplacedByFluid(state)) return false;
        for(Direction direction : Direction.values()){
            if(ignoreDownwardTest.getAsBoolean() && direction == Direction.DOWN) continue;
            if(isContainingLiquid(world.getBlockState(pos.offset(direction)))) return false;
        }
        return true;
    }
    private boolean isStackOk(ItemStack stack){
        Item item = stack.getItem();
        if(blacklistItems.contains(item)) return false;
        if (!(item instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        if (block.getHardness() != 0) return false;
        BlockState state = block.getDefaultState();
        if (canBeReplacedByFluid(state)) return false;
        FluidState fluidState = state.getFluidState();
        if (fluidState.getLevel() != 0) return false;
        Collection<Property<?>> properties = state.getProperties();
        if (properties.contains(Properties.WATERLOGGED)) return false;
        if (properties.contains(Properties.AGE_1)) return false;
        if (properties.contains(Properties.AGE_2)) return false;
        if (properties.contains(Properties.AGE_3)) return false;
        if (properties.contains(Properties.AGE_4)) return false;
        if (properties.contains(Properties.AGE_5)) return false;
        if (properties.contains(Properties.AGE_7)) return false;
        if (properties.contains(Properties.AGE_15)) return false;
        return !properties.contains(Properties.AGE_25);
    }
}
