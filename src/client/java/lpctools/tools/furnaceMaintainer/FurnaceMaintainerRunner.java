package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.DataUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

// 或许可以预测syncId提前发送interact网络包以加速？
public class FurnaceMaintainerRunner implements QuietAutoCloseable, ClientTickEvents.EndTick {
    double operationReserved = 0;
    long lastInteractTimeMillis = 0;
    @Nullable BlockPos lastInteractedPos = null;
    FurnaceMaintainerRunner(){ registerAll(true); }
    @Override public void close(){ registerAll(false); }
    private void registerAll(boolean b){Registries.END_CLIENT_TICK.register(this, b);}
    
    @Override public void onEndTick(MinecraftClient mc) {
        if(runner != this) {
            close();
            return;
        }
        else if(dataInstance == null) {
			runner = null;
            FMConfig.setBooleanValue(false);
			close();
			return;
		}
        
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        ClientPlayerInteractionManager itm = mc.interactionManager;
        if(player == null || world == null || itm == null) {
            FMConfig.setBooleanValue(false);
            return;
        }
        
        operationReserved += operationSpeedLimit.getDoubleValue();
        
        if(lastInteractedPos != null){
            if(System.currentTimeMillis() - lastInteractTimeMillis > 1000) {
                FMConfig.setBooleanValue(false);
                DataUtils.clientMessage(Text.translatable("lpctools.configs.tools.FM.interactionMismatch"), true);
            }
        }
        if(MinecraftClient.getInstance().options.sneakKey.isPressed()) return;
        int requiredEmptyStackCount = includesHopperAbove.getBooleanValue() ? 5 : 1;
        for(ItemStack stack : player.getInventory().main){
            if(stack.isEmpty()){
                if(--requiredEmptyStackCount <= 0)
                    break;
            }
        }
        if(requiredEmptyStackCount > 0) {
            FMConfig.setBooleanValue(false);
            DataUtils.clientMessage(Text.translatable("lpctools.configs.tools.FM.notEnoughEmptyStack").getString(), true);
            return;
        }
        if(operationReserved < 1) return;
        if(lastInteractedPos == null) {
            for(BlockPos pos : reachDistance.iterateFromClosest(player.getEyePos())){
                if(!dataInstance.highlightInstance.containsKey(pos)) continue;
                var state = world.getBlockState(pos);
                var block = state.getBlock();
                if(block instanceof AbstractFurnaceBlock) {
                    var upperPos = pos.up();
                    if(dataInstance.highlightInstance.containsKey(upperPos)) {
                        var upperState = world.getBlockState(upperPos);
                        if(upperState.getBlock() instanceof HopperBlock && upperState.get(HopperBlock.FACING) == Direction.DOWN) continue;
                    }
                }
                else if(!(block instanceof HopperBlock) || state.get(HopperBlock.FACING) != Direction.DOWN
					|| !(world.getBlockState(pos.down()).getBlock() instanceof AbstractFurnaceBlock)) continue;
                BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, lastInteractedPos = pos.toImmutable(), false);
                isFMInteracting = true;
                itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
                isFMInteracting = false;
                lastInteractTimeMillis = System.currentTimeMillis();
                --operationReserved;
                break;
            }
        }
        if(operationReserved > 1) operationReserved = 1;
    }
}
