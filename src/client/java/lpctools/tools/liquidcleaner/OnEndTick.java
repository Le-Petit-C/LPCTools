package lpctools.tools.liquidcleaner;

import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static lpctools.tools.liquidcleaner.LiquidCleaner.*;
import static lpctools.util.BlockStateUtils.*;

public class OnEndTick implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            disable();
            return;
        }
        BlockPos playerBlock = player.getBlockPos();
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            disable();
            return;
        }
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if (itm == null) {
            disable();
            return;
        }
        for (int a = -5; a <= 5; ++a) {
            for (int b = -5; b <= 5; ++b) {
                for (int c = -5; c <= 5; ++c) {
                    BlockPos pos = playerBlock.add(a, b, c);
                    Vec3d midPos = pos.toCenterPos();
                    if (!HandRestock.restock(placeableItems)) return;
                    if (midPos.subtract(player.getPos()).length() >= 4.5) continue;
                    if (shouldAttackBlock(world, pos))
                        itm.attackBlock(pos, Direction.UP);
                }
            }
        }
        if (HandRestock.search(placeableItems) == -1) return;
        for (int a = -5; a <= 5; ++a) {
            for (int b = -5; b <= 5; ++b) {
                for (int c = -5; c <= 5; ++c) {
                    BlockPos pos = playerBlock.add(a, b, c);
                    Vec3d midPos = pos.toCenterPos();
                    if (!HandRestock.restock(placeableItems)) return;
                    if (midPos.subtract(player.getPos()).length() >= 4.5) continue;
                    BlockState state = world.getBlockState(pos);
                    if (isReplaceableLiquid(state)) {
                        BlockHitResult hitResult = new BlockHitResult(midPos, Direction.UP, pos, false);
                        itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
                    }
                }
            }
        }
    }

    private static boolean shouldAttackBlock(@NotNull ClientWorld world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        if(!isZeroHardBlock(state)) return false;
        if(state.isAir()) return false;
        if(!isReplaceable(state) && isContainingLiquid(state)) return true;
        if(isContainingLiquid(world.getBlockState(pos.west()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.east()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.down()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.up()))) return false;
        if(isContainingLiquid(world.getBlockState(pos.north()))) return false;
        return !isContainingLiquid(world.getBlockState(pos.south()));
    }
}
