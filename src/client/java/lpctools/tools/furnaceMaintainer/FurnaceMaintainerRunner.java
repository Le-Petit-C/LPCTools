package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import lpctools.util.inGame.InGameManager;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HopperBlock;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

// 或许可以预测syncId提前发送interact网络包以加速？
public class FurnaceMaintainerRunner implements QuietAutoCloseable, ClientTickEvents.EndTick, Registries.ContainerContentInitializedCallback, ToolUtils.ToolRunner {
    double operationReserved = 0;
    long lastInteractTimeMillis = 0;
    @Nullable BlockPos lastInteractedPos = null;
    @Override public void close(){ registerAll(false); }
    @Override public void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
        Registries.CLIENT_CONTAINER_CONTENT_INITIALIZED.register(this, b);
    }
    
    @Override public void onEndTick(@NonNull Minecraft mc) {
        if(dataInstance == null) return;
        InGameManager manager = InGameManager.get(mc);
        if(manager == null) {
            FMConfig.setBooleanValue(false);
            return;
        }
        
        operationReserved += operationSpeedLimit.getDoubleValue();
        
        if(lastInteractedPos != null){
            if(System.currentTimeMillis() - lastInteractTimeMillis > 1000) {
                FMConfig.setBooleanValue(false);
                DataUtils.clientMessage(Component.translatable("lpctools.configs.tools.FM.interactionMismatch"), true);
            }
        }
        if(Minecraft.getInstance().hasShiftDown()) return;
        int requiredEmptyStackCount = includesHopperAbove.getBooleanValue() ? 5 : 1;
        for(ItemStack stack : manager.getInventory().getNonEquipmentItems()){
            if(stack.isEmpty()){
                if(--requiredEmptyStackCount <= 0)
                    break;
            }
        }
        if(requiredEmptyStackCount > 0) {
            FMConfig.setBooleanValue(false);
            DataUtils.clientMessage(Component.translatable("lpctools.configs.tools.FM.notEnoughEmptyStack").getString(), true);
            return;
        }
        if(operationReserved < 1) return;
        if(lastInteractedPos == null) {
            for(BlockPos pos : reachDistance.iterateFromClosest(manager.playerEyePos())){
                if(!dataInstance.highlightInstance.containsKey(pos)) continue;
                var state = manager.getBlockState(pos);
                var block = state.getBlock();
                if(block instanceof AbstractFurnaceBlock) {
                    var upperPos = pos.above();
                    if(dataInstance.highlightInstance.containsKey(upperPos)) {
                        var upperState = manager.getBlockState(upperPos);
                        if(upperState.getBlock() instanceof HopperBlock && upperState.getValue(HopperBlock.FACING) == Direction.DOWN) continue;
                    }
                }
                else if(!(block instanceof HopperBlock) || state.getValue(HopperBlock.FACING) != Direction.DOWN
					|| !(manager.getBlockState(pos.below()).getBlock() instanceof AbstractFurnaceBlock)) continue;
                isFMInteracting = true;
                manager.useItemOn(InteractionHand.MAIN_HAND, pos);
                isFMInteracting = false;
                lastInteractedPos = pos.immutable();
                lastInteractTimeMillis = System.currentTimeMillis();
                --operationReserved;
                break;
            }
        }
        if(operationReserved > 1) operationReserved = 1;
    }

    @Override public void onContainerContentInitialized(AbstractContainerMenu menu) {
        if(dataInstance == null || lastInteractedPos == null) return;
        InGameManager manager = InGameManager.get();
        if(manager == null) {
            FMConfig.setBooleanValue(false);
            return;
        }
        boolean operated;
        if(menu instanceof FurnaceMenu furnaceMenu) {
            manager.handleContainerInput(furnaceMenu.containerId, 0, 0, ContainerInput.QUICK_MOVE);
            operated = true;
        }
        else if(menu instanceof HopperMenu hopperMenu) {
            for(int i = 0; i < 5; ++i) manager.handleContainerInput(hopperMenu.containerId, i, 0, ContainerInput.QUICK_MOVE);
            operated = true;
        }
        else operated = false;
        if(operated) {
            manager.closeContainer();
            dataInstance.highlightInstance.mark(lastInteractedPos, null);
            lastInteractedPos = null;
        }
    }
}
