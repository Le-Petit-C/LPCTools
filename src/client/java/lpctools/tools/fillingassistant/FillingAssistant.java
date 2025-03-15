package lpctools.tools.fillingassistant;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.block.Block;
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
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.FA_enableNotification")), true);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!enabled()) return;
        Registry.unregisterEndClientTickCallback(runner);
        Registry.unregisterInGameEndMouseCallback(runner);
        runner = null;
        ToolConfigs.displayDisableReason("FA_disableNotification", reasonKey);
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
            if(limitFillingRange.getAsBoolean()){
                if(pos.getX() < minXConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
                if(pos.getX() > maxXConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
                if(pos.getY() < minYConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
                if(pos.getY() > maxYConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
                if(pos.getZ() < minZConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
                if(pos.getZ() > maxZConfig.getAsInt()) return outerRangeBlockMethod.getCurrentUserdata().isBlockUnpassable(block);
            }
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
        hotkeyConfig = FAConfig.addHotkeyConfig("FA_Hotkey", "", new HotkeyCallback());
        limitPlaceSpeedConfig = FAConfig.addThirdListConfig("FA_limitPlaceSpeed", false);
        maxBlockPerTickConfig = limitPlaceSpeedConfig.addDoubleConfig("FA_maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = FAConfig.addDoubleConfig("FA_reachDistance", 4.5, 0, 5);
        testDistanceConfig = FAConfig.addIntegerConfig("FA_testDistance", 6, 6, 64, new TestDistanceRefreshCallback());
        disableOnLeftDownConfig = FAConfig.addBooleanConfig("FA_disableOnLeftDown", true);
        disableOnGUIOpened = FAConfig.addBooleanConfig("FA_disableOnGUIOpened", false);
        placeableItemsConfig = FAConfig.addStringListConfig("FA_placeableItems", defaultPlaceableItemIdList, new PlaceableItemsRefreshCallback());
        passableBlocksConfig = FAConfig.addStringListConfig("FA_passableBlocks", defaultPassableBlockIdList, new PassableBlocksRefreshCallback());
        transparentAsPassableConfig = FAConfig.addBooleanConfig("FA_transparentAsPassable", true);
        notOpaqueAsPassableConfig = FAConfig.addBooleanConfig("FA_notOpaqueAsPassable", true);
        requiredBlocksConfig = FAConfig.addStringListConfig("FA_requiredBlocks", defaultRequiredBlockIdList, new RequiredBlocksRefreshCallback());
        offhandFillingConfig = FAConfig.addBooleanConfig("FA_OffhandFilling", false);
        limitFillingRange = FAConfig.addThirdListConfig("FA_LimitFillingRange", false);
        outerRangeBlockMethod = limitFillingRange.addOptionListConfig("FA_OuterRangeBlockMethod");
        outerRangeBlockMethod.addOption(outerRangeBlockMethods.AS_UNPASSABLE.getKey(), outerRangeBlockMethods.AS_UNPASSABLE);
        outerRangeBlockMethod.addOption(outerRangeBlockMethods.AS_PASSABLE.getKey(), outerRangeBlockMethods.AS_PASSABLE);
        outerRangeBlockMethod.addOption(outerRangeBlockMethods.AS_ORIGIN.getKey(), outerRangeBlockMethods.AS_ORIGIN);
        minXConfig = limitFillingRange.addIntegerConfig("FA_minX", Integer.MIN_VALUE);
        maxXConfig = limitFillingRange.addIntegerConfig("FA_maxX", Integer.MAX_VALUE);
        minYConfig = limitFillingRange.addIntegerConfig("FA_minY", Integer.MIN_VALUE);
        maxYConfig = limitFillingRange.addIntegerConfig("FA_maxY", Integer.MAX_VALUE);
        minZConfig = limitFillingRange.addIntegerConfig("FA_minZ", Integer.MIN_VALUE);
        maxZConfig = limitFillingRange.addIntegerConfig("FA_maxZ", Integer.MAX_VALUE);
        valueChangeConfig = limitFillingRange.addIntegerListConfig("FA_ValueChange");
        valueChangeConfig.addOption("minX", minXConfig);
        valueChangeConfig.addOption("maxX", maxXConfig);
        valueChangeConfig.addOption("minY", minYConfig);
        valueChangeConfig.addOption("maxY", maxYConfig);
        valueChangeConfig.addOption("minZ", minZConfig);
        valueChangeConfig.addOption("maxZ", maxZConfig);
        valueAddHotkeyConfig = limitFillingRange.addHotkeyConfig("FA_AddValueKey", "",
                new HotkeyConfig.IntegerChanger<>(1, valueChangeConfig, limitFillingRange));
        valueSubtractHotkeyConfig = limitFillingRange.addHotkeyConfig("FA_SubtractValueKey", "",
                new HotkeyConfig.IntegerChanger<>(-1, valueChangeConfig, limitFillingRange));
    }

    enum outerRangeBlockMethods{
        AS_UNPASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asUnpassable", (Block block) -> true),
        AS_PASSABLE("lpctools.configs.tools.outerRangeBlockMethods.asPassable", (Block block) -> false),
        AS_ORIGIN("lpctools.configs.tools.outerRangeBlockMethods.asOrigin", FillingAssistant::isBlockUnpassable);
        public final String translationKey;
        public final Method method;
        public interface Method{ boolean isBlockUnpassable(Block block);}
        outerRangeBlockMethods(String translationKey, Method method){
            this.translationKey = translationKey;
            this.method = method;
        }
        String getKey(){return translationKey;}
        boolean isBlockUnpassable(Block block){return method.isBlockUnpassable(block);}
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
    static ThirdListConfig limitFillingRange;
    static OptionListConfig<outerRangeBlockMethods> outerRangeBlockMethod;
    static IntegerConfig minXConfig;
    static IntegerConfig maxXConfig;
    static IntegerConfig minYConfig;
    static IntegerConfig maxYConfig;
    static IntegerConfig minZConfig;
    static IntegerConfig maxZConfig;
    static IntegerListConfig<IntegerConfig> valueChangeConfig;
    static HotkeyConfig valueAddHotkeyConfig;
    static HotkeyConfig valueSubtractHotkeyConfig;
    @NotNull private static HashSet<Item> itemSetFromIdList(@Nullable List<String> list){
        HashSet<Item> ret = new HashSet<>();
        if(list == null) return ret;
        for(String s : list)
            ret.add(Registries.ITEM.get(Identifier.tryParse(s)));
        return ret;
    }
    @NotNull private static HashSet<Block> blockSetFromIdList(@Nullable List<String> list){
        HashSet<Block> ret = new HashSet<>();
        if(list == null) return ret;
        for(String s : list)
            ret.add(Registries.BLOCK.get(Identifier.tryParse(s)));
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
