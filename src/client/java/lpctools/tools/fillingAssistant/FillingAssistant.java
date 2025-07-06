package lpctools.tools.fillingAssistant;

import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.fillingAssistant.FillingAssistantData.*;

public class FillingAssistant {
    public static final ThirdListConfig FAConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "FA", false);
    static {listStack.push(FAConfig);}
    public static final BooleanHotkeyConfig fillingAssistant = addBooleanHotkeyConfig("fillingAssistant", false, null, FillingAssistant::fillingAssistantConfigCallback);
    static {fillingAssistant.getKeybind().setCallback(new KeyCallbackToggleBoolean(fillingAssistant));}
    public static final LimitOperationSpeedConfig limitPlaceSpeedConfig = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig(FillingAssistant::reachDistanceConfigCallback);
    public static final IntegerConfig testDistanceConfig = addIntegerConfig("testDistance", 6, 6, 64, FillingAssistant::testDistanceChangeCallback);
    public static final BooleanConfig disableOnLeftDownConfig = addBooleanConfig("disableOnLeftDown", true);
    public static final BooleanConfig disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
    public static final ObjectListConfig.BlockItemListConfig placeableItemsConfig = addBlockItemListConfig("placeableItems", defaultPlaceableItemList);
    public static final ObjectListConfig.BlockListConfig passableBlocksConfig = addBlockListConfig("passableBlocks", defaultPassableBlockList);
    public static final BooleanConfig transparentAsPassableConfig = addBooleanConfig("transparentAsPassable", true);
    public static final BooleanConfig notOpaqueAsPassableConfig = addBooleanConfig("notOpaqueAsPassable", true);
    public static final ObjectListConfig.BlockListConfig requiredBlocksConfig = addBlockListConfig("requiredBlocks", defaultRequiredBlockWhiteList);
    public static final BooleanConfig offhandFillingConfig = addBooleanConfig("offhandFilling", false);
    public static final RangeLimitConfig limitFillingRange = addRangeLimitConfig(false);
    public static final ArrayOptionListConfig<OuterRangeBlockMethod> outerRangeBlockMethod = addArrayOptionListConfig(limitFillingRange, "outerRangeBlockMethod", outerRangeBlockMethods);
    static {listStack.pop();}
    
    private static void fillingAssistantConfigCallback() {
        if(fillingAssistant.getBooleanValue()) enableTool();
        else disableTool(null);
    }
    private static void reachDistanceConfigCallback(){testDistanceConfig.setMin((int)reachDistanceConfig.getAsDouble() + 1);}
    private static void testDistanceChangeCallback(){if(runner != null) runner.setTestDistance(testDistanceConfig.getAsInt());}
    
    public static void enableTool(){
        if(runner != null) return;
        runner = new PlaceBlockTick();
        fillingAssistant.setBooleanValue(true);
        Registries.END_CLIENT_TICK.register(runner);
        Registries.IN_GAME_END_MOUSE.register(runner);
        displayEnableMessage(fillingAssistant);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(runner == null) return;
        Registries.END_CLIENT_TICK.unregister(runner);
        Registries.IN_GAME_END_MOUSE.unregister(runner);
        runner = null;
        fillingAssistant.setBooleanValue(false);
        displayDisableReason(fillingAssistant, reasonKey);
    }
    public static @NotNull HashSet<BlockItem> getPlaceableItems(){return placeableItemsConfig.set;}
    public static @NotNull HashSet<Block> getPassableBlocks(){return passableBlocksConfig.set;}
    public static boolean isBlockUnpassable(Block block){
        if(transparentAsPassableConfig.getAsBoolean() && block.getDefaultState().isTransparent()) return false;
        if(notOpaqueAsPassableConfig.getAsBoolean() && !block.getDefaultState().isOpaque()) return false;
        return !getPassableBlocks().contains(block);
    }
    public static boolean isUnpassable(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null){
            Block block = world.getBlockState(pos).getBlock();
            return isBlockUnpassable(block);
        }
        else return true;
    }
    public static @NotNull HashSet<Block> getRequiredBlocks(){return requiredBlocksConfig.set;}
    public static boolean required(Block block){return getRequiredBlocks().contains(block);}
    public static boolean required(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) return required(world.getBlockState(pos).getBlock());
        else return false;
    }
    public interface OuterRangeBlockMethod {
        boolean isBlockUnpassable(Block block);
        default boolean isUnpassable(BlockPos pos, @Nullable BlockView world){
            if(world != null) return isBlockUnpassable(world.getBlockState(pos).getBlock());
            else return isBlockUnpassable(Blocks.VOID_AIR);
        }
    }

    @Nullable private static PlaceBlockTick runner = null;
}
