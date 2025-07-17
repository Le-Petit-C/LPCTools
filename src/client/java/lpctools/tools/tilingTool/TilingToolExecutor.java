package lpctools.tools.tilingTool;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import lpctools.util.javaex.Object2BooleanFunction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashSet;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.tilingTool.TilingTool.*;
import static lpctools.tools.tilingTool.TilingToolData.*;

public class TilingToolExecutor implements AutoCloseable, ClientTickEvents.EndTick{
    TilingToolExecutor(){
        registerAll(true);
        if(autoRefresh.get().refreshOnToolEnabled)
            refreshCallback();
    }
    @Override public void close() {registerAll(false);}
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    @Override public void onEndTick(MinecraftClient mc) {
        if(storedData == null) {
            if(autoRefresh.get().refreshOnExecuteNull)
                refreshCallback();
            if(storedData == null) return;
        }
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
        MutableObject<Block> block = new MutableObject<>();
        Object2BooleanFunction<Block> condition = b ->{
            Block storedBlock = block.getValue();
            if(b == storedBlock) return true;
            ArrayList<HashSet<Block>> list = vagueBlocks.get(storedBlock);
            if(list == null) return false;
            for(HashSet<Block> set : list)
                if(set.contains(b)) return true;
            return false;
        };
        HandRestock.IRestockTest restockTest = stack->{
            if(!(stack.getItem() instanceof BlockItem blockItem)) return false;
            return condition.getBoolean(blockItem.getBlock());
        };
        limitOperationSpeed.resetOperationTimes();
        limitOperationSpeed.iterableOperate(reachDistance.iterateFromClosest(player.getEyePos()), pos->{
            if(!world.getBlockState(pos).isReplaceable()) return NO_OPERATION;
            BlockPos.Mutable shiftPos = new BlockPos.Mutable();
            shiftPos.set(pos.subtract(startPos));
            if(!tilingDirection.booleans.get(0).getBooleanValue() && (shiftPos.getX() < 0 || shiftPos.getX() >= cuboidSize.getX())) return NO_OPERATION;
            if(!tilingDirection.booleans.get(1).getBooleanValue() && (shiftPos.getY() < 0 || shiftPos.getY() >= cuboidSize.getY())) return NO_OPERATION;
            if(!tilingDirection.booleans.get(2).getBooleanValue() && (shiftPos.getZ() < 0 || shiftPos.getZ() >= cuboidSize.getZ())) return NO_OPERATION;
            MathUtils.clamp(shiftPos, cuboidSize);
            block.setValue(storedBlocks[shiftPos.getZ()][shiftPos.getY()][shiftPos.getX()]);
            if(data.block == null){
                data.count = HandRestock.restock(restockTest, 0);
                if(data.count == 0) return NO_OPERATION;
                data.block = block.getValue();
            }
            if(!condition.getBoolean(data.block)) return NO_OPERATION;
            BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos.toImmutable(), false);
            itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
            if(--data.count == 0) return SHOULD_BREAK;
            else return OPERATED;
        });
    }
}
