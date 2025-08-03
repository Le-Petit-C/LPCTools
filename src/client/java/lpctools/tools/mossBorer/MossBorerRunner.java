package lpctools.tools.mossBorer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.BlockUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.PriorityQueue;

import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.mossBorer.MossBorer.*;

public class MossBorerRunner implements AutoCloseable, ClientTickEvents.EndTick{
    MossBorerRunner(){
        registerAll(true);
    }
    @Override public void close() {
        registerAll(false);
    }
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    
    @Override public void onEndTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ClientPlayerInteractionManager itm = mc.interactionManager;
        if(player == null || itm == null) {
            MBConfig.setBooleanValue(false);
            return;
        }
        operationSpeed.resetOperationTimes();
        ClientWorld world = player.clientWorld;
        Vec3d eyePos = player.getEyePos();
        Comparator<BlockPos> comparator = (pos1, pos2)-> pos2.getY() != pos1.getY() ?
            pos2.getY() - pos1.getY() :
            Double.compare(pos2.getSquaredDistance(eyePos), pos1.getSquaredDistance(eyePos));
        PriorityQueue<BlockPos> mossBlocks = new PriorityQueue<>(comparator);
        operationSpeed.iterableOperate(reachDistance.iterateFromClosest(player.getEyePos()), pos->{
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() == Blocks.MOSS_BLOCK) {
                mossBlocks.add(pos.toImmutable());
                return NO_OPERATION;
            }
            if(state.isAir()) return NO_OPERATION;
            if(BlockUtils.canBreakInstantly(player, pos)) {
                itm.attackBlock(pos, Direction.UP);
                return OPERATED;
            }
            return NO_OPERATION;
        });
        while(mossBlocks.size() > 1){
            BlockPos pos = mossBlocks.poll();
            if(pos.getY() - player.getBlockY() < -2) break;
            if(BlockUtils.canBreakInstantly(player, pos)) {
                if(!operationSpeed.next()) break;
                itm.attackBlock(pos, Direction.UP);
            }
        }
        if(!mossBlocks.isEmpty() && operationSpeed.next() && HandRestock.restock(stack->stack.getItem() == Items.BONE_MEAL, -1) != 0){
            BlockPos pos = mossBlocks.poll();
            assert pos != null;
            BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos.toImmutable(), false);
            itm.interactBlock(player, Hand.OFF_HAND, hitResult);
        }
    }
}
