package lpctools.tools.antiSpawner;

import com.google.common.collect.ImmutableList;
import lpctools.compact.derived.ShapeList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static lpctools.generic.GenericUtils.*;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.util.BlockUtils.*;
import static lpctools.util.DataUtils.*;

//TODO:
// 未抑制方块显示
public class AntiSpawner implements ClientTickEvents.EndTick {
    public static BooleanHotkeyConfig antiSpawnerConfig;
    public static StringListConfig placeableItemIds;
    public static ReachDistanceConfig reachDistanceConfig;
    public static RangeLimitConfig rangeLimitConfig;
    public static LimitOperationSpeedConfig limitOperationSpeedConfig;
    public static void init() {
        antiSpawnerConfig = addBooleanHotkeyConfig("antiSpawner", false, null, ()->{
            if(antiSpawnerConfig.getBooleanValue()) start();
            else stop();
        });
        setLPCToolsToggleText(antiSpawnerConfig);
        limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
        reachDistanceConfig = addReachDistanceConfig();
        placeableItemIds = addStringListConfig("placeableItems", idListFromItemList(defaultPlaceableItems),
            ()->itemSetFromIds(placeableItemIds.get(), placeableItems, true));
        rangeLimitConfig = addRangeLimitConfig(false);
    }
    private static final AntiSpawner instance = new AntiSpawner();
    public static void start(){Registry.registerEndClientTickCallback(instance);}
    public static void stop(){Registry.unregisterEndClientTickCallback(instance);}
    public static final ImmutableList<Item> defaultPlaceableItems;
    public static final ArrayList<Item> placeableItems;
    static {
        placeableItems = new ArrayList<>();
        for(Block block : Registries.BLOCK){
            Item item;
            try{item = block.asItem();}
            catch (Throwable ignored){continue;}
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
                limitOperationSpeedConfig.limitWithRestock(restockTest, 0);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
                    pos.toBottomCenterPos(), Direction.UP, pos.offset(Direction.DOWN), false
                ));
                return OPERATED;
            });
    }
    private static final HandRestock.IRestockTest restockTest = item -> placeableItems.contains(item.getItem());
}
