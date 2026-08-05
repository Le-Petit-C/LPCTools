package lpctools.tools.tilingTool;

import com.google.common.collect.ImmutableSet;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import lpctools.util.inGame.InGameManager;
import lpctools.util.javaex.Object2BooleanFunction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.Predicate;

import static lpctools.tools.tilingTool.TilingTool.*;
import static lpctools.tools.tilingTool.TilingToolData.*;

public class TilingToolRunner implements ToolUtils.ToolRunner, ClientTickEvents.EndTick{
    TilingToolRunner(){
        registerAll(true);
        if(autoRefresh.get().refreshOnToolEnabled)
            autoRefreshOperation.get().run();
    }
    @Override public void registerAll(boolean b){ Registries.END_CLIENT_TICK.register(this, b); }
    @Override public void onEndTick(@NonNull Minecraft mc) {
        if(mc.isPaused()) return;
        if(storedData == null) {
            if(autoRefresh.get().refreshOnExecuteNull)
                autoRefreshOperation.get().run();
            if(storedData == null) return;
        }
        InGameManager manager = InGameManager.get(mc);
        if(manager == null) {
            TTConfig.setBooleanValue(false);
            return;
        }
        Vec3i cuboidSize = storedData.cuboidSize();
        BlockPos startPos = storedData.startPos();
        Block[][][] storedBlocks = storedData.storedBlocks();
        Block[] blockAtPos = new Block[1];
        Block blockInHand = null;
        Object2BooleanFunction<Block> blockFitsBlockAtPos = b ->{
            if(b == blockAtPos[0]) return true;
            ArrayList<ImmutableSet<Block>> list = vagueBlocks.get(blockAtPos[0]);
            if(list == null) return false;
            for(var set : list) if(set.contains(b)) return true;
            return false;
        };
        Predicate<ItemStack> restockTest = stack->{
            if(!(stack.getItem() instanceof BlockItem blockItem)) return false;
            return blockFitsBlockAtPos.getBoolean(blockItem.getBlock());
        };
        var limit = OperationSpeedLimit.root().limitWithRestock(restockTest, interactionHand.offhandPriority());
        for(BlockPos pos : reachDistance.iterateFromClosest(manager.playerEyePos())) {
            if(!limit.hasReservedTimesRegardlessRestock()) break;
            if(!shapeList.testPos(pos)) continue;
            if(!manager.getBlockState(pos).canBeReplaced()) continue;
            BlockPos.MutableBlockPos shiftPos = new BlockPos.MutableBlockPos();
            shiftPos.set(pos.subtract(startPos));
            if(!tilingDirection.booleans.get(0).getBooleanValue() && (shiftPos.getX() < 0 || shiftPos.getX() >= cuboidSize.getX())) continue;
            if(!tilingDirection.booleans.get(1).getBooleanValue() && (shiftPos.getY() < 0 || shiftPos.getY() >= cuboidSize.getY())) continue;
            if(!tilingDirection.booleans.get(2).getBooleanValue() && (shiftPos.getZ() < 0 || shiftPos.getZ() >= cuboidSize.getZ())) continue;
            MathUtils.clamp(shiftPos, cuboidSize);
            blockAtPos[0] = storedBlocks[shiftPos.getZ()][shiftPos.getY()][shiftPos.getX()];
            if(blockInHand == null) {
                if(HandRestock.search(restockTest, interactionHand.offhandPriority()) == -1) continue;
                blockInHand = blockAtPos[0];
            }
            if(!blockFitsBlockAtPos.getBoolean(blockInHand)) continue;
            limit.costInteractBlock();
            manager.useItemOn(interactionHand.getHand(), pos);
        }
    }
}
