package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

// 或许可以预测syncId提前发送interact网络包以加速？
public class FurnaceMaintainerRunner implements QuietAutoCloseable, ClientTickEvents.EndTick, ToolUtils.ToolRunner, Registries.BeforeScreenChangeCallback {
    double operationReserved = 0;
    long lastInteractTimeMillis = 0;
    @Nullable BlockPos lastInteractedPos = null;
    @Override public void close(){ registerAll(false); }
    @Override public void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
        Registries.BEFORE_SCREEN_CHANGE.register(this, b);
    }
    
    @Override public void onEndTick(@NonNull Minecraft mc) {
        if(runner != this) {
            close();
            return;
        }
        else if(dataInstance == null) return;
        
        LocalPlayer player = mc.player;
        ClientLevel world = mc.level;
        MultiPlayerGameMode itm = mc.gameMode;
        if(player == null || world == null || itm == null) {
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
        for(ItemStack stack : player.getInventory().getNonEquipmentItems()){
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
            for(BlockPos pos : reachDistance.iterateFromClosest(player.getEyePosition())){
                if(!dataInstance.highlightInstance.containsKey(pos)) continue;
                var state = world.getBlockState(pos);
                var block = state.getBlock();
                if(block instanceof AbstractFurnaceBlock) {
                    var upperPos = pos.above();
                    if(dataInstance.highlightInstance.containsKey(upperPos)) {
                        var upperState = world.getBlockState(upperPos);
                        if(upperState.getBlock() instanceof HopperBlock && upperState.getValue(HopperBlock.FACING) == Direction.DOWN) continue;
                    }
                }
                else if(!(block instanceof HopperBlock) || state.getValue(HopperBlock.FACING) != Direction.DOWN
					|| !(world.getBlockState(pos.below()).getBlock() instanceof AbstractFurnaceBlock)) continue;
                BlockHitResult hitResult = new BlockHitResult(pos.getCenter(), Direction.DOWN, lastInteractedPos = pos.immutable(), false);
                isFMInteracting = true;
                itm.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
                isFMInteracting = false;
                lastInteractTimeMillis = System.currentTimeMillis();
                --operationReserved;
                break;
            }
        }
        if(operationReserved > 1) operationReserved = 1;
    }

    // 清理熔炉，熔炉/漏斗为空点一下也没事，不用等熔炉/漏斗menu物品初始化完毕。
    @Override public boolean beforeScreenChange(Screen newScreen) {
        if(!FMConfig.getBooleanValue() || dataInstance == null || runner == null || runner.lastInteractedPos == null) return false;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        MultiPlayerGameMode itm = client.gameMode;
        if(player == null || itm == null) {
            FMConfig.setBooleanValue(false);
            return false;
        }
        boolean operated;
        if(newScreen instanceof AbstractFurnaceScreen<?> screen1) {
            itm.handleContainerInput(screen1.getMenu().containerId, 0, 0, ContainerInput.QUICK_MOVE, player);
            operated = true;
        }
        else if(newScreen instanceof HopperScreen screen1) {
            for(int i = 0; i < 5; ++i) itm.handleContainerInput(screen1.getMenu().containerId, i, 0, ContainerInput.QUICK_MOVE, player);
            operated = true;
        }
        else operated = false;
        if(operated) {
            closeClientContainer(newScreen);
            dataInstance.highlightInstance.mark(runner.lastInteractedPos, null);
            runner.lastInteractedPos = null;
        }
        return operated;
    }
    private static void closeClientContainer(Screen screen){
        var mc = Minecraft.getInstance();
        if(mc.gui.screen() == screen && mc.player instanceof LocalPlayer player)
            player.closeContainer();
    }
}
