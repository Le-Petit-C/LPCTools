package lpctools.tools.fillingassistant;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
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
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.fillingAssistant.enableNotification")), true);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!enabled()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        Registry.unregisterEndClientTickCallback(runner);
        Registry.unregisterInGameEndMouseCallback(runner);
        runner = null;
        String reason = StringUtils.translate("lpctools.tools.fillingAssistant.disableNotification");
        if(reasonKey != null) reason += " : " + StringUtils.translate(reasonKey);
        player.sendMessage(Text.literal(reason), true);
    }
    public static boolean enabled(){return runner != null;}
    public static HashSet<Item> getPlaceableItems(){return placeableItems;}
    public static boolean placeable(Item item){return getPlaceableItems().contains(item);}
    public static HashSet<Block> getPassableBlocks(){return passableBlocks;}
    public static boolean passable(Block block){
        if(transparentAsPassableConfig.getValue() && block.getDefaultState().isTransparent()) return true;
        if(notOpaqueAsPassableConfig.getValue() && !block.getDefaultState().isOpaque()) return true;
        return getPassableBlocks().contains(block);
    }
    public static boolean unpassable(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null)return !passable(world.getBlockState(pos).getBlock());
        else return true;
    }
    public static HashSet<Block> getRequiredBlocks(){return requiredBlocks;}
    public static boolean required(Block block){return getRequiredBlocks().contains(block);}
    public static boolean required(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) return required(world.getBlockState(pos).getBlock());
        else return false;
    }
    public static boolean replaceable(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null) return false;
        return world.getBlockState(pos).isReplaceable();
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
        maxBlockPerTick = limitPlaceSpeedConfig.addDoubleConfig("FA_maxBlockPerTick", 1.0);
        disableOnLeftDownConfig = FAConfig.addBooleanConfig("FA_disableOnLeftDown", true);
        disableOnGUIOpened = FAConfig.addBooleanConfig("FA_disableOnGUIOpened", false);
        placeableItemsConfig = FAConfig.addStringListConfig("FA_placeableItems", defaultPlaceableItemIdList, new PlaceableItemsRefreshCallback());
        passableBlocksConfig = FAConfig.addStringListConfig("FA_passableBlocks", defaultPassableBlockIdList, new PassableBlocksRefreshCallback());
        transparentAsPassableConfig = FAConfig.addBooleanConfig("FA_transparentAsPassable", true);
        notOpaqueAsPassableConfig = FAConfig.addBooleanConfig("FA_notOpaqueAsPassable", true);
        requiredBlocksConfig = FAConfig.addStringListConfig("FA_requiredBlocks", defaultRequiredBlockIdList, new RequiredBlocksRefreshCallback());
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
    static DoubleConfig maxBlockPerTick;
    static BooleanConfig disableOnLeftDownConfig;
    static BooleanConfig disableOnGUIOpened;
    static StringListConfig placeableItemsConfig;
    static StringListConfig passableBlocksConfig;
    static BooleanConfig transparentAsPassableConfig;
    static BooleanConfig notOpaqueAsPassableConfig;
    static StringListConfig requiredBlocksConfig;
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
    private static PlaceBlockTick runner = null;
    private static HashSet<Item> placeableItems;
    private static HashSet<Block> passableBlocks;
    private static HashSet<Block> requiredBlocks;

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if(enabled()) disableTool(null);
            else enableTool();
            return true;
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
