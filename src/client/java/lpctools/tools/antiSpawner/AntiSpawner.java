package lpctools.tools.antiSpawner;

import com.google.common.collect.ImmutableList;
import lpctools.compact.derived.ShapeList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static lpctools.generic.GenericUtils.*;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.util.BlockUtils.*;
import static lpctools.util.DataUtils.*;

//TODO:非下蹲状态忽略可交互方块
public class AntiSpawner extends ThirdListConfig implements ClientTickEvents.EndTick {
    public final AntiSpawnerSwitch antiSpawnerConfig = addConfig(new AntiSpawnerSwitch());
    public final ObjectListConfig.BlockItemListConfig placeableItems;
    public final ReachDistanceConfig reachDistanceConfig;
    public final RangeLimitConfig rangeLimitConfig;
    public final LimitOperationSpeedConfig limitOperationSpeedConfig;
    public class AntiSpawnerSwitch extends BooleanHotkeyConfig{
        public AntiSpawnerSwitch() {super(AntiSpawner.this, "antiSpawner", false, null);}
        @Override public void onValueChanged() {
            super.onValueChanged();
            if(getBooleanValue()) start();
            else stop();
        }
    }
    public AntiSpawner(ILPCConfigList parent) {
        super(parent, "AS", false);
        try(ConfigListLayer ignored = new ConfigListLayer(this)){
            setLPCToolsToggleText(antiSpawnerConfig);
            limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
            reachDistanceConfig = addReachDistanceConfig();
            placeableItems = addBlockItemListConfig(this, "placeableItems", defaultPlaceableItems);
            rangeLimitConfig = addRangeLimitConfig(false);
            restockTest = item -> item.getItem() instanceof BlockItem blockItem && placeableItems.set.contains(blockItem);
        }
    }
    public void start(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.register(this);}
    public void stop(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.unregister(this);}
    public static final ImmutableList<BlockItem> defaultPlaceableItems;
    static {
        ArrayList<BlockItem> placeableItems = new ArrayList<>();
        for(Block block : Registries.BLOCK){
            BlockItem item;
            try{item = (BlockItem) block.asItem();}
            catch (Exception ignored){continue;}
            if(canBeReplacedByFluid(block)) continue;
            if(block.getDefaultState().isBurnable()) continue;
            String idPath = getBlockId(block);
            if(idPath.contains("rail")) placeableItems.add(item);
            else if(idPath.contains("slab")) placeableItems.add(item);
        }
        defaultPlaceableItems = ImmutableList.copyOf(placeableItems);
    }
    @Override public void onEndTick(MinecraftClient mc) {
        if(mc.player == null || mc.world == null || mc.interactionManager == null){
            antiSpawnerConfig.setBooleanValue(false);
            return;
        }
        if(mc.currentScreen != null) return;
        if(HandRestock.search(restockTest, 0) == -1) return;
        ShapeList shapeList = rangeLimitConfig.buildShapeList();
        limitOperationSpeedConfig.resetOperationTimes();
        //默认遍历的距离判断是与方块中心的距离，但是这里选择interact底下方块的上表面中心，所以添加了一个y+0.5的偏移修正
        limitOperationSpeedConfig.iterableOperate(
            reachDistanceConfig.iterateFromClosest(mc.player.getEyePos().add(0, 0.5, 0)),
            pos->{
                if(!shapeList.testPos(pos)) return NO_OPERATION;
                if(!mayMobSpawnAt(mc.world, mc.world.getLightingProvider(), pos)) return NO_OPERATION;
                if(!mc.world.getBlockState(pos).isReplaceable()) return NO_OPERATION;
                BlockPos downPos = pos.down();
                BlockPos hitPos;
                if(mc.world.getBlockState(pos.down()).isReplaceable()) hitPos = pos;
                else hitPos = downPos;
                BlockHitResult hitResult = new BlockHitResult(
                    pos.toBottomCenterPos(), Direction.UP, hitPos, false);
                if(!mc.player.isSneaking()){
                    BlockState below = mc.world.getBlockState(pos.down());
                    ActionResult result = below.onUse(mc.world, mc.player, hitResult);
                    if(result == ActionResult.SUCCESS) {
                        notifyPlayer(String.format("onUse at %s", pos.down().toString()), false);
                        return NO_OPERATION;
                    }
                }
                limitOperationSpeedConfig.limitWithRestock(restockTest, 0);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                return OPERATED;
            });
    }
    private final HandRestock.IRestockTest restockTest;
}
