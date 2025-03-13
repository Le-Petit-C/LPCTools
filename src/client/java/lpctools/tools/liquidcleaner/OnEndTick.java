package lpctools.tools.liquidcleaner;

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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static lpctools.tools.liquidcleaner.LiquidCleaner.*;
import static lpctools.util.BlockUtils.*;
import static lpctools.util.LPCMathUtils.*;

public class OnEndTick implements ClientTickEvents.EndTick {
    @Override public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            disableTool("nullClientPlayerEntity");
            return;
        }
        BlockPos playerEyeBlock = new BlockPos(floorVec3d(player.getEyePos()));
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
        int r = (int)Math.ceil(d);
        int minX = playerEyeBlock.getX() - r;
        int maxX = playerEyeBlock.getX() + r;
        int minY = playerEyeBlock.getY() - r;
        int maxY = playerEyeBlock.getY() + r;
        int minZ = playerEyeBlock.getZ() - r;
        int maxZ = playerEyeBlock.getZ() + r;
        if(limitCleaningRange.getAsBoolean()){
            if(minX < minXConfig.getAsInt()) minX = minXConfig.getAsInt();
            if(maxX > maxXConfig.getAsInt()) maxX = maxXConfig.getAsInt();
            if(minY < minYConfig.getAsInt()) minY = minYConfig.getAsInt();
            if(maxY > maxYConfig.getAsInt()) maxY = maxYConfig.getAsInt();
            if(minZ < minZConfig.getAsInt()) minZ = minZConfig.getAsInt();
            if(maxZ > maxZConfig.getAsInt()) maxZ = maxZConfig.getAsInt();
        }
        DimensionType dimensionType = client.world.getDimension();
        if(minY < dimensionType.minY()) minY = dimensionType.minY();
        if(maxY > dimensionType.minY() + dimensionType.height() - 1) maxY = dimensionType.minY() + dimensionType.height() - 1;
        if(limitInteractSpeedConfig.getAsBoolean()){
            if(canInteractBlockCount > 1) canInteractBlockCount = 0;
            canInteractBlockCount += maxBlockPerTickConfig.getAsDouble();
        }
        else canInteractBlockCount = Double.MAX_VALUE;
        for (int y = maxY; y >= minY; --y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Vec3d midPos = pos.toCenterPos();
                    if (midPos.subtract(player.getEyePos()).length() >= d) continue;
                    if (shouldAttackBlock(world, pos)){
                        manager.attackBlock(pos, Direction.UP);
                        if(--canInteractBlockCount < 1) return;
                    }
                }
            }
        }
        if (HandRestock.search(IsPlaceableItem.instance, 0) == -1) return;
        for (int y = maxY; y >= minY; --y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Vec3d midPos = pos.toCenterPos();
                    if (midPos.subtract(player.getEyePos()).length() >= d) continue;
                    BlockState state = world.getBlockState(pos);
                    if (isReplaceableLiquid(state)) {
                        if (!HandRestock.restock(IsPlaceableItem.instance, 0)) return;
                        BlockHitResult hitResult = new BlockHitResult(midPos, Direction.UP, pos, false);
                        manager.interactBlock(player, Hand.MAIN_HAND, hitResult);
                        if(--canInteractBlockCount < 1) return;
                    }
                }
            }
        }
    }

    private double canInteractBlockCount = 0;
    private static boolean shouldAttackBlock(@NotNull ClientWorld world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        if(!isZeroHardBlock(state)) return false;
        if(state.isAir()) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(canBeReplacedByFluid(state)) return false;
        if(isContainingLiquid(world.getBlockState(pos.west()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.east()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.down()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.up()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.north()))) return false;
        return !isContainingLiquid(world.getBlockState(pos.south()));
    }
    private static class IsPlaceableItem implements HandRestock.IRestockTest{
        @Override public boolean isStackOk(ItemStack stack){
            Item item = stack.getItem();
            if(!(item instanceof BlockItem blockItem)) return false;
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
            if (properties.contains(Properties.AGE_25)) return false;
            return true;
        }
        public static final IsPlaceableItem instance = new IsPlaceableItem();
    }
}
