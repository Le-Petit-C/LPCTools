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

import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

public class FurnaceMaintainerRunner implements QuietAutoCloseable, ClientTickEvents.EndTick {
    FurnaceMaintainerRunner(){registerAll(true);}
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
        int requiredEmptyStackCount = includesHopperAbove.getBooleanValue() ? 5 : 1;
        for(ItemStack stack : player.getInventory().getMainStacks()){
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
        for(BlockPos pos : reachDistance.iterateFromClosest(player.getEyePos())){
            if(dataInstance.highlightInstance.containsKey(pos)){
                var state = world.getBlockState(pos);
                var block = state.getBlock();
                boolean shouldClick;
                if(block instanceof AbstractFurnaceBlock) {
                    var upperPos = pos.up();
                    if(dataInstance.highlightInstance.containsKey(upperPos)) {
                        var upperState = world.getBlockState(upperPos);
                        shouldClick = !(upperState.getBlock() instanceof HopperBlock) || upperState.get(HopperBlock.FACING) != Direction.DOWN;
                    }
                    else shouldClick = true;
                }
                else shouldClick = block instanceof HopperBlock && state.get(HopperBlock.FACING) == Direction.DOWN
					&& world.getBlockState(pos.down()).getBlock() instanceof AbstractFurnaceBlock;
                if(shouldClick) {
                    BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos.toImmutable(), false);
                    itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
                    dataInstance.highlightInstance.mark(pos, null);
                    break;
                }
            }
        }
    }
}
