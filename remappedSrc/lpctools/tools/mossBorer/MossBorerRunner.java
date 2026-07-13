package lpctools.tools.mossBorer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.BlockUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
    
    @Override public void onEndTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        MultiPlayerGameMode itm = mc.gameMode;
        if(player == null || itm == null) {
            MBConfig.setBooleanValue(false);
            return;
        }
        operationSpeed.resetOperationTimes();
        Level world = player.level();
        Vec3 eyePos = player.getEyePosition();
        Comparator<BlockPos> comparator = (pos1, pos2)-> pos2.getY() != pos1.getY() ?
            pos2.getY() - pos1.getY() :
            Double.compare(pos2.distToCenterSqr(eyePos), pos1.distToCenterSqr(eyePos));
        PriorityQueue<BlockPos> mossBlocks = new PriorityQueue<>(comparator);
        operationSpeed.iterableOperate(reachDistance.iterateFromClosest(player.getEyePosition()), pos->{
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() == Blocks.MOSS_BLOCK) {
                mossBlocks.add(pos.immutable());
                return NO_OPERATION;
            }
            if(state.isAir()) return NO_OPERATION;
            if(BlockUtils.canBreakInstantly(player, pos)) {
                itm.startDestroyBlock(pos, Direction.UP);
                return OPERATED;
            }
            return NO_OPERATION;
        });
        while(mossBlocks.size() > 1){
            BlockPos pos = mossBlocks.poll();
            if(pos.getY() - player.getBlockY() < -2) break;
            if(BlockUtils.canBreakInstantly(player, pos)) {
                if(!operationSpeed.next()) break;
                itm.startDestroyBlock(pos, Direction.UP);
            }
        }
        if(!mossBlocks.isEmpty() && operationSpeed.next() && HandRestock.restock(stack->stack.getItem() == Items.BONE_MEAL, -1) != 0){
            BlockPos pos = mossBlocks.poll();
            assert pos != null;
            BlockHitResult hitResult = new BlockHitResult(pos.getCenter(), Direction.UP, pos.immutable(), false);
            itm.useItemOn(player, InteractionHand.OFF_HAND, hitResult);
        }
    }
}
