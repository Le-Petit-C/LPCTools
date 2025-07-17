package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.AlgorithmUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.AbstractFurnaceBlock;
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
import static lpctools.util.DataUtils.notifyPlayer;

public class FurnaceMaintainerRunner implements AutoCloseable, ClientTickEvents.EndTick {
    FurnaceMaintainerRunner(){
        registerAll(true);
    }
    @Override public void close(){
        registerAll(false);
    }
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    @Override public void onEndTick(MinecraftClient mc) {
        AlgorithmUtils.fastRemove(detectTasks, task->{
            if(!task.isDone()) return false;
            uncheckedFurnaces.addAll(task.join());
            return true;
        });
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        ClientPlayerInteractionManager itm = mc.interactionManager;
        if(player == null || world == null || itm == null) {
            FMConfig.setBooleanValue(false);
            return;
        }
        boolean hasEmptyStack = false;
        for(ItemStack stack : player.getInventory().getMainStacks()){
            if(stack.isEmpty()){
                hasEmptyStack = true;
                break;
            }
        }
        if(!hasEmptyStack) {
            notifyPlayer(Text.translatable("lpctools.configs.tools.FM.noEmptyStack").getString(), true);
            return;
        }
        for(BlockPos pos : reachDistance.iterateFromClosest(player.getEyePos())){
            if(uncheckedFurnaces.contains(pos) && world.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock){
                BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos.toImmutable(), false);
                itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
                uncheckedFurnaces.remove(pos);
                break;
            }
        }
    }
}
