package lpctools.tools.fillingAssistant;

import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.fillingAssistant.Data.*;
import static lpctools.util.DataUtils.*;

public class FillingAssistant {
    public static void enableTool(){
        if(runner != null) return;
        runner = new PlaceBlockTick();
        fillingAssistant.setBooleanValue(true);
        Registry.registerEndClientTickCallback(runner);
        Registry.registerInGameEndMouseCallback(runner);
        displayEnableMessage(fillingAssistant);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(runner == null) return;
        Registry.unregisterEndClientTickCallback(runner);
        Registry.unregisterInGameEndMouseCallback(runner);
        runner = null;
        fillingAssistant.setBooleanValue(false);
        displayDisableReason(fillingAssistant, reasonKey);
    }
    public static @NotNull HashSet<Item> getPlaceableItems(){return placeableItems;}
    public static @NotNull HashSet<Block> getPassableBlocks(){return passableBlocks;}
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
    public static @NotNull HashSet<Block> getRequiredBlocks(){return requiredBlocks;}
    public static boolean required(Block block){return getRequiredBlocks().contains(block);}
    public static boolean required(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) return required(world.getBlockState(pos).getBlock());
        else return false;
    }
    public static void init(){
        fillingAssistant = addBooleanHotkeyConfig("fillingAssistant", false, "", ()->onMainValueChanged(fillingAssistant.getBooleanValue()));
        fillingAssistant.getKeybind().setCallback(new KeyCallbackToggleBoolean(fillingAssistant));
        limitPlaceSpeedConfig = addThirdListConfig("limitPlaceSpeed", false);
        maxBlockPerTickConfig = addDoubleConfig(limitPlaceSpeedConfig, "maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = addReachDistanceConfig(
                ()->testDistanceConfig.setMin((int)reachDistanceConfig.getAsDouble() + 1)
        );
        testDistanceConfig = addIntegerConfig("testDistance", 6, 6, 64, new TestDistanceChangeCallback());
        disableOnLeftDownConfig = addBooleanConfig("disableOnLeftDown", true);
        disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
        placeableItemsConfig = addStringListConfig("placeableItems", defaultPlaceableItemIdList, () -> placeableItems = itemSetFromIds(placeableItemsConfig.getStrings()));
        passableBlocksConfig = addStringListConfig("passableBlocks", defaultPassableBlockIdList, () -> passableBlocks = blockSetFromIds(passableBlocksConfig.getStrings()));
        transparentAsPassableConfig = addBooleanConfig("transparentAsPassable", true);
        notOpaqueAsPassableConfig = addBooleanConfig("notOpaqueAsPassable", true);
        requiredBlocksConfig = addStringListConfig("requiredBlocks", defaultRequiredBlockIdList, () -> requiredBlocks = blockSetFromIds(requiredBlocksConfig.getStrings()));
        offhandFillingConfig = addBooleanConfig("offhandFilling", false);
        limitFillingRange = addRangeLimitConfig(false, "FA");
        outerRangeBlockMethod = addArrayOptionListConfig(limitFillingRange, "outerRangeBlockMethod");
        for(OuterRangeBlockMethod method : OuterRangeBlockMethod.values())
            outerRangeBlockMethod.addOption(method.getKey(), method);
    }

    enum OuterRangeBlockMethod {
        AS_UNPASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asUnpassable", block -> true),
        AS_PASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asPassable", block -> false),
        AS_ORIGIN("lpctools.configs.tools.outerRangeBlockMethods.asOrigin", FillingAssistant::isBlockUnpassable);
        public final String translationKey;
        public final Method method;
        public interface Method{ boolean isBlockUnpassable(Block block);}
        OuterRangeBlockMethod(String translationKey, Method method){
            this.translationKey = translationKey;
            this.method = method;
        }
        String getKey(){return translationKey;}
        boolean isBlockUnpassable(Block block){return method.isBlockUnpassable(block);}
        boolean isUnpassable(BlockPos pos){
            ClientWorld world = MinecraftClient.getInstance().world;
            if(world != null) return isBlockUnpassable(world.getBlockState(pos).getBlock());
            else return isBlockUnpassable(Blocks.VOID_AIR);
        }
    }

    static BooleanHotkeyConfig fillingAssistant;
    static ThirdListConfig limitPlaceSpeedConfig;
    static DoubleConfig maxBlockPerTickConfig;
    static ReachDistanceConfig reachDistanceConfig;
    static IntegerConfig testDistanceConfig;
    static BooleanConfig disableOnLeftDownConfig;
    static BooleanConfig disableOnGUIOpened;
    static StringListConfig placeableItemsConfig;
    static StringListConfig passableBlocksConfig;
    static BooleanConfig transparentAsPassableConfig;
    static BooleanConfig notOpaqueAsPassableConfig;
    static StringListConfig requiredBlocksConfig;
    static BooleanConfig offhandFillingConfig;
    static RangeLimitConfig limitFillingRange;
    static ArrayOptionListConfig<OuterRangeBlockMethod> outerRangeBlockMethod;
    @Nullable private static PlaceBlockTick runner = null;
    @NotNull private static HashSet<Item> placeableItems = new HashSet<>(defaultPlaceableItemList);
    @NotNull private static HashSet<Block> passableBlocks = new HashSet<>(defaultPassableBlockList);
    @NotNull private static HashSet<Block> requiredBlocks = new HashSet<>(defaultRequiredBlockWhiteList);

    public static void onMainValueChanged(boolean currentValue) {
        if(currentValue) enableTool();
        else disableTool(null);
    }
    private static class TestDistanceChangeCallback implements ILPCValueChangeCallback {
        @Override public void onValueChanged() {
            if(runner != null)
                runner.setTestDistance(testDistanceConfig.getAsInt());
        }
    }
}
