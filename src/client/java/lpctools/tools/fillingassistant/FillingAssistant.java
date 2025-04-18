package lpctools.tools.fillingassistant;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

import static lpctools.tools.fillingassistant.Data.*;

public class FillingAssistant {
    public static void enableTool(){
        if(enabled()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        runner = new PlaceBlockTick();
        Registry.registerEndClientTickCallback(runner);
        Registry.registerInGameEndMouseCallback(runner);
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.FA.enableNotification")), true);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!enabled()) return;
        Registry.unregisterEndClientTickCallback(runner);
        Registry.unregisterInGameEndMouseCallback(runner);
        runner = null;
        ToolConfigs.displayDisableReason("FA.disableNotification", reasonKey);
    }
    public static boolean enabled(){return runner != null;}
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
    public static void refreshPlaceableItems(){
        if(placeableItemsConfig != null) placeableItems = itemSetFromIdList(placeableItemsConfig.getStrings());
        else placeableItems = new HashSet<>(defaultPlaceableItemList);
    }
    public static void refreshPassableBlocks(){
        if(passableBlocksConfig != null) passableBlocks = blockSetFromIdList(passableBlocksConfig.getStrings());
        else passableBlocks = new HashSet<>(defaultPassableBlockList);
    }
    public static void refreshRequiredBlocks(){
        if(requiredBlocksConfig != null) requiredBlocks = blockSetFromIdList(requiredBlocksConfig.getStrings());
        else requiredBlocks = new HashSet<>(defaultRequiredBlockWhiteList);
    }
    public static void init(ThirdListConfig FAConfig){
        hotkeyConfig = FAConfig.addHotkeyConfig("hotkey", "", new HotkeyCallback());
        limitPlaceSpeedConfig = FAConfig.addThirdListConfig("limitPlaceSpeed", false);
        maxBlockPerTickConfig = limitPlaceSpeedConfig.addDoubleConfig("maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = FAConfig.addDoubleConfig("reachDistance", 4.5, 0, 5);
        testDistanceConfig = FAConfig.addIntegerConfig("testDistance", 6, 6, 64, new TestDistanceRefreshCallback());
        disableOnLeftDownConfig = FAConfig.addBooleanConfig("disableOnLeftDown", true);
        disableOnGUIOpened = FAConfig.addBooleanConfig("disableOnGUIOpened", false);
        placeableItemsConfig = FAConfig.addStringListConfig("placeableItems", defaultPlaceableItemIdList, new PlaceableItemsRefreshCallback());
        passableBlocksConfig = FAConfig.addStringListConfig("passableBlocks", defaultPassableBlockIdList, new PassableBlocksRefreshCallback());
        transparentAsPassableConfig = FAConfig.addBooleanConfig("transparentAsPassable", true);
        notOpaqueAsPassableConfig = FAConfig.addBooleanConfig("notOpaqueAsPassable", true);
        requiredBlocksConfig = FAConfig.addStringListConfig("requiredBlocks", defaultRequiredBlockIdList, new RequiredBlocksRefreshCallback());
        offhandFillingConfig = FAConfig.addBooleanConfig("offhandFilling", false);
        limitFillingRange = FAConfig.addRangeLimitConfig(false, "FA");
        outerRangeBlockMethod = limitFillingRange.addOptionListConfig("outerRangeBlockMethod");
        for(OuterRangeBlockMethods method : OuterRangeBlockMethods.values())
            outerRangeBlockMethod.addOption(method.getKey(), method);
    }

    enum OuterRangeBlockMethods {
        AS_UNPASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asUnpassable", block -> true),
        AS_PASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asPassable", block -> false),
        AS_ORIGIN("lpctools.configs.tools.outerRangeBlockMethods.asOrigin", FillingAssistant::isBlockUnpassable);
        public final String translationKey;
        public final Method method;
        public interface Method{ boolean isBlockUnpassable(Block block);}
        OuterRangeBlockMethods(String translationKey, Method method){
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

    /*
    //这是个为了测试onValueChanged的callback
    private static class testCallback implements IValueChangeCallback<ConfigStringList>{
        @Override
        public void onValueChanged(ConfigStringList config) {
            LPCAPIInit.LOGGER.info("Change detected!");
        }
    }
    */

    static HotkeyConfig hotkeyConfig;
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
    static OptionListConfig<OuterRangeBlockMethods> outerRangeBlockMethod;
    @NotNull private static HashSet<Item> itemSetFromIdList(@Nullable List<String> list){
        HashSet<Item> ret = new HashSet<>();
        if(list == null) return ret;
        for(String s : list)
            ret.add(Registries.ITEM.get(Identifier.of(s)));
        return ret;
    }
    @NotNull private static HashSet<Block> blockSetFromIdList(@Nullable List<String> list){
        HashSet<Block> ret = new HashSet<>();
        if(list == null) return ret;
        for(String s : list)
            ret.add(Registries.BLOCK.get(Identifier.of(s)));
        return ret;
    }
    @Nullable private static PlaceBlockTick runner = null;
    @NotNull private static HashSet<Item> placeableItems = new HashSet<>();
    @NotNull private static HashSet<Block> passableBlocks = new HashSet<>();
    @NotNull private static HashSet<Block> requiredBlocks = new HashSet<>();

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if(enabled()) disableTool(null);
            else enableTool();
            return true;
        }
    }
    private static class TestDistanceRefreshCallback implements IValueRefreshCallback{
        @Override public void valueRefreshCallback() {
            if(runner != null)
                runner.setTestDistance(testDistanceConfig.getAsInt());
        }
    }
    private static class PlaceableItemsRefreshCallback implements IValueRefreshCallback{
        @Override public void valueRefreshCallback() {
            refreshPlaceableItems();
        }
    }
    private static class PassableBlocksRefreshCallback implements IValueRefreshCallback{
        @Override public void valueRefreshCallback() {
            refreshPassableBlocks();
        }
    }
    private static class RequiredBlocksRefreshCallback implements IValueRefreshCallback{
        @Override public void valueRefreshCallback() {
            refreshRequiredBlocks();
        }
    }
}
