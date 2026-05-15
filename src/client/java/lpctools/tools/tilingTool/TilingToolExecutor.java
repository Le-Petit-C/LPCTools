package lpctools.tools.tilingTool;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import lpctools.util.javaex.Object2BooleanFunction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.tilingTool.TilingTool.*;
import static lpctools.tools.tilingTool.TilingToolData.*;

public class TilingToolExecutor implements AutoCloseable, ClientTickEvents.EndTick{
    TilingToolExecutor(){
        registerAll(true);
        if(autoRefresh.get().refreshOnToolEnabled)
            autoRefreshOperation.get().run();
    }
    @Override public void close() {registerAll(false);}
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    @Override public void onEndTick(@NonNull Minecraft mc) {
        if(storedData == null) {
            if(autoRefresh.get().refreshOnExecuteNull)
                autoRefreshOperation.get().run();
            if(storedData == null) return;
        }
        class Data{
            Block block = null;
            int count = 0;
        }
        Data data = new Data();
        LocalPlayer player = mc.player;
        MultiPlayerGameMode itm = mc.gameMode;
        ClientLevel world = mc.level;
        if(player == null || itm == null || world == null) return;
        Vec3i cuboidSize = storedData.cuboidSize();
        BlockPos startPos = storedData.startPos();
        Block[][][] storedBlocks = storedData.storedBlocks();
        MutableObject<Block> block = new MutableObject<>();
        Object2BooleanFunction<Block> condition = b ->{
            Block storedBlock = block.getValue();
            if(b == storedBlock) return true;
            ArrayList<ImmutableSet<Block>> list = vagueBlocks.get(storedBlock);
            if(list == null) return false;
            for(var set : list)
                if(set.contains(b)) return true;
            return false;
        };
        HandRestock.IRestockTest restockTest = stack->{
            if(!(stack.getItem() instanceof BlockItem blockItem)) return false;
            return condition.getBoolean(blockItem.getBlock());
        };
        limitOperationSpeed.resetOperationTimes();
        limitOperationSpeed.iterableOperate(reachDistance.iterateFromClosest(player.getEyePosition()), pos->{
            if(!shapeList.testPos(pos)) return NO_OPERATION;
            if(!world.getBlockState(pos).canBeReplaced()) return NO_OPERATION;
            BlockPos.MutableBlockPos shiftPos = new BlockPos.MutableBlockPos();
            shiftPos.set(pos.subtract(startPos));
            if(!tilingDirection.booleans.get(0).getBooleanValue() && (shiftPos.getX() < 0 || shiftPos.getX() >= cuboidSize.getX())) return NO_OPERATION;
            if(!tilingDirection.booleans.get(1).getBooleanValue() && (shiftPos.getY() < 0 || shiftPos.getY() >= cuboidSize.getY())) return NO_OPERATION;
            if(!tilingDirection.booleans.get(2).getBooleanValue() && (shiftPos.getZ() < 0 || shiftPos.getZ() >= cuboidSize.getZ())) return NO_OPERATION;
            MathUtils.clamp(shiftPos, cuboidSize);
            block.setValue(storedBlocks[shiftPos.getZ()][shiftPos.getY()][shiftPos.getX()]);
            if(data.block == null){
                data.count = HandRestock.restock(restockTest, offhandOperate.getAsBoolean() ? -1 : 0);
                if(data.count == 0) return NO_OPERATION;
                data.block = block.getValue();
            }
            if(!condition.getBoolean(data.block)) return NO_OPERATION;
            BlockHitResult hitResult = new BlockHitResult(pos.getCenter(), Direction.DOWN, pos.immutable(), false);
            itm.useItemOn(player, offhandOperate.getAsBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, hitResult);
            if(--data.count == 0) return SHOULD_BREAK;
            else return OPERATED;
        });
    }
}
