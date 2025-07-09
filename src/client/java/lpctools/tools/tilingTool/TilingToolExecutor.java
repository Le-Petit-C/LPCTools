package lpctools.tools.tilingTool;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.tilingTool.TilingTool.*;
import static lpctools.tools.tilingTool.TilingToolData.storedData;

public class TilingToolExecutor implements AutoCloseable, ClientTickEvents.EndTick{
    TilingToolExecutor(){registerAll(true);}
    @Override public void close() {registerAll(false);}
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    @Override public void onEndTick(MinecraftClient mc) {
        if(storedData == null) return;
        class Data{
            Block block = null;
            int count = 0;
        }
        Data data = new Data();
        ClientPlayerEntity player = mc.player;
        ClientPlayerInteractionManager itm = mc.interactionManager;
        ClientWorld world = mc.world;
        if(player == null || itm == null || world == null) return;
        Vec3i cuboidSize = storedData.cuboidSize();
        BlockPos startPos = storedData.startPos();
        Block[][][] storedBlocks = storedData.storedBlocks();
        limitOperationSpeed.resetOperationTimes();
        limitOperationSpeed.iterableOperate(reachDistance.iterateFromClosest(player.getEyePos()), pos->{
            if(!world.getBlockState(pos).isReplaceable()) return NO_OPERATION;
            BlockPos.Mutable shiftPos = new BlockPos.Mutable();
            shiftPos.set(pos.subtract(startPos));
            if(shiftPos.getY() < 0 || shiftPos.getY() >= cuboidSize.getY()) return NO_OPERATION;
            MathUtils.clamp(shiftPos, cuboidSize);
            Block block = storedBlocks[shiftPos.getZ()][shiftPos.getY()][shiftPos.getX()];
            if(data.block == null){
                Item item = block.asItem();
                if(item == null) return NO_OPERATION;
                data.count = HandRestock.restock(stack->item.equals(stack.getItem()), 0);
                if(data.count == 0) return NO_OPERATION;
                data.block = block;
            }
            if(!data.block.equals(block)) return NO_OPERATION;
            BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos.toImmutable(), false);
            itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
            if(--data.count == 0) return SHOULD_BREAK;
            else return OPERATED;
        });
    }
}
