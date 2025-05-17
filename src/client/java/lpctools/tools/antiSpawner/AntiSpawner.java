package lpctools.tools.antiSpawner;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.util.AlgorithmUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static lpctools.generic.GenericUtils.*;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.util.BlockUtils.*;
import static lpctools.util.DataUtils.*;

//TODO: 未抑制方块显示
public class AntiSpawner implements ClientTickEvents.EndTick {
    public static BooleanHotkeyConfig antiSpawnerConfig;
    public static StringListConfig placeableItemIds;
    public static DoubleConfig reachDistanceConfig;
    public static void init() {
        antiSpawnerConfig = addBooleanHotkeyConfig("antiSpawner", false, null, ()->{
            if(antiSpawnerConfig.getBooleanValue()) start();
            else stop();
        });
        setLPCToolsToggleText(antiSpawnerConfig);
        reachDistanceConfig = addDoubleConfig("reachDistance", 4.5, 0, 5);
        placeableItemIds = addStringListConfig("placeableItems", idListFromItemList(defaultPlaceableItems));
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
        for(BlockPos pos : AlgorithmUtils.iterateInNears(mc.player.getEyePos(), reachDistanceConfig.getAsDouble())){
            BlockPos offsetPos = pos.offset(Direction.UP);
            if(!mayMobSpawnAt(mc.world, offsetPos)) continue;
            if(!mc.world.getBlockState(offsetPos).isReplaceable()) continue;
            if(!HandRestock.restock(item -> placeableItems.contains(item.getItem()), 0)) break;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
                    pos.toCenterPos(), Direction.UP, pos.mutableCopy(), false
            ));
        }
    }
}
