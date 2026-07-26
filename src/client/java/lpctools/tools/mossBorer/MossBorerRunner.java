package lpctools.tools.mossBorer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.BlockUtils;
import lpctools.util.inGame.InGameManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.PriorityQueue;

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
    
    @Override public void onEndTick(@NonNull Minecraft mc) {
        InGameManager manager = InGameManager.get(mc);
        if(manager == null) {
            MBConfig.setBooleanValue(false);
            return;
        }
        Vec3 eyePos = manager.playerEyePos();
        Comparator<BlockPos> comparator = (pos1, pos2)-> pos2.getY() != pos1.getY() ?
            pos2.getY() - pos1.getY() :
            Double.compare(pos2.distToCenterSqr(eyePos), pos1.distToCenterSqr(eyePos));
        PriorityQueue<BlockPos> mossBlocks = new PriorityQueue<>(comparator);
        var limit = limitOperationSpeedConfig.getLimit();
        for(BlockPos pos : reachDistance.iterateFromClosest(eyePos)) {
            if(!limit.hasReservedTimes()) break;
            BlockState state = manager.getBlockState(pos);
            if(state.getBlock() == Blocks.MOSS_BLOCK) {
                mossBlocks.add(pos.immutable());
                continue;
            }
            if(state.isAir()) continue;
            if(BlockUtils.canBreakInstantly(manager.player, pos)) {
                limit.costBreakBlock();
                manager.startDestroyBlock(pos, Direction.UP);
            }
        }
        while(mossBlocks.size() > 1){
            BlockPos pos = mossBlocks.poll();
            if(pos.getY() - manager.player.getBlockY() < -2) break;
            if(BlockUtils.canBreakInstantly(manager.player, pos)) {
                if(!limit.hasReservedTimes()) break;
                limit.costBreakBlock();
                manager.startDestroyBlock(pos, Direction.UP);
            }
        }
        if(mossBlocks.poll() instanceof BlockPos pos) {
            var restockedLimit = limit.limitWithRestock(stack->stack.getItem() == Items.BONE_MEAL, -1);
            if(restockedLimit.hasReservedTimes()) {
                restockedLimit.costInteractBlock();
                manager.useItemOn(InteractionHand.OFF_HAND, pos);
            }
        }
    }
}
