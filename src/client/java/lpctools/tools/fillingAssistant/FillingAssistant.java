package lpctools.tools.fillingAssistant;

import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

import static lpctools.tools.fillingAssistant.Data.*;
import static lpctools.util.DataUtils.*;

public class FillingAssistant {
    public static void enableTool(){
        if(runner != null) return;
        runner = new PlaceBlockTick();
        fillingAssistant.setBooleanValue(true);
        Registry.registerEndClientTickCallback(runner);
        Registry.registerInGameEndMouseCallback(runner);
        ToolConfigs.displayEnableMessage(fillingAssistant);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(runner == null) return;
        Registry.unregisterEndClientTickCallback(runner);
        Registry.unregisterInGameEndMouseCallback(runner);
        runner = null;
        fillingAssistant.setBooleanValue(false);
        ToolConfigs.displayDisableReason(fillingAssistant, reasonKey);
    }
    public static @NotNull HashSet<Item> getPlaceableItems(){return placeableItems;}
    public static @NotNull HashSet<Block> getPassableBlocks(){return passableBlocks;}
    public static boolean isBlockUnpassable(Block block){
        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null) return false;
        if(transparentAsPassableConfig.getAsBoolean() && block.getDefaultState().isTransparent(world, BlockPos.ORIGIN)) return false;
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
    public static void init(ThirdListConfig FAConfig){
        fillingAssistant = FAConfig.addBooleanHotkeyConfig("fillingAssistant", false, "", ()->onMainValueChanged(fillingAssistant.getBooleanValue()));
        fillingAssistant.getKeybind().setCallback(new KeyCallbackToggleBoolean(fillingAssistant));
        limitPlaceSpeedConfig = FAConfig.addThirdListConfig("limitPlaceSpeed", false);
        maxBlockPerTickConfig = limitPlaceSpeedConfig.addDoubleConfig("maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = FAConfig.addDoubleConfig("reachDistance", 4.5, 0, 5);
        testDistanceConfig = FAConfig.addIntegerConfig("testDistance", 6, 6, 64, new TestDistanceChangeCallback());
        disableOnLeftDownConfig = FAConfig.addBooleanConfig("disableOnLeftDown", true);
        disableOnGUIOpened = FAConfig.addBooleanConfig("disableOnGUIOpened", false);
        placeableItemsConfig = FAConfig.addStringListConfig("placeableItems", defaultPlaceableItemIdList, () -> placeableItems = itemSetFromIds(placeableItemsConfig.getStrings()));
        passableBlocksConfig = FAConfig.addStringListConfig("passableBlocks", defaultPassableBlockIdList, () -> passableBlocks = blockSetFromIds(passableBlocksConfig.getStrings()));
        transparentAsPassableConfig = FAConfig.addBooleanConfig("transparentAsPassable", true);
        notOpaqueAsPassableConfig = FAConfig.addBooleanConfig("notOpaqueAsPassable", true);
        requiredBlocksConfig = FAConfig.addStringListConfig("requiredBlocks", defaultRequiredBlockIdList, () -> requiredBlocks = blockSetFromIds(requiredBlocksConfig.getStrings()));
        offhandFillingConfig = FAConfig.addBooleanConfig("offhandFilling", false);
        limitFillingRange = FAConfig.addRangeLimitConfig(false, "FA");
        OptionListConfig.OptionList<OuterRangeBlockMethod> optionList = new OptionListConfig.OptionList<>();
        for(OuterRangeBlockMethod method : OuterRangeBlockMethod.values())
            optionList.addOption(method.getKey(), method);
        outerRangeBlockMethod = limitFillingRange.addOptionListConfig("outerRangeBlockMethod", optionList.getFirst());
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
    static DoubleConfig reachDistanceConfig;
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
    static OptionListConfig<OuterRangeBlockMethod> outerRangeBlockMethod;
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
