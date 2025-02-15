package lpctools.tools.fillingassistant;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.DoubleConfig;
import lpctools.lpcfymasaapi.configbutton.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.StringListConfig;
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
    public static void switchPlaceMode(){
        if(enabled()) disableTool(null);
        else enableTool();
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
    public static boolean passable(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null)return passable(world.getBlockState(pos).getBlock());
        else return false;
    }
    public static HashSet<Block> getRequiredBlocks(){return requiredBlocks;}
    public static boolean required(Block block){return getRequiredBlocks().contains(block);}
    public static boolean required(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) return required(world.getBlockState(pos).getBlock());
        else return false;
    }
    @NotNull public static IHotkeyCallback getHotkeyCallback(){return new HotkeyCallback();}
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
    public static void refresh(){refreshPlaceableItems();refreshPassableBlocks();refreshRequiredBlocks();}
    public static void init(LPCConfigList list){
        if(initialized) return;
        hotkeyConfig = list.addHotkeyConfig("FA", "", getHotkeyCallback());
        limitPlaceSpeedConfig = list.addBooleanConfig("FA_limitPlaceSpeed", false, new LimitPlaceSpeedCallback());
        maxPlaceSpeedPerTick = list.addDoubleConfig("FA_maxPlaceSpeedPerTick", 1.0);
        maxPlaceSpeedPerTick.enabled = false;
        disableOnLeftDownConfig = list.addBooleanConfig("FA_disableOnLeftDown", true);
        disableOnGUIOpened = list.addBooleanConfig("FA_disableOnGUIOpened", false);
        placeableItemsConfig = list.addStringListConfig("FA_placeableItems", defaultPlaceableItemIdList);
        passableBlocksConfig = list.addStringListConfig("FA_passableBlocks", defaultPassableBlockIdList);
        transparentAsPassableConfig = list.addBooleanConfig("FA_transparentAsPassable", true);
        notOpaqueAsPassableConfig = list.addBooleanConfig("FA_notOpaqueAsPassable", true);
        requiredBlocksConfig = list.addStringListConfig("FA_requiredBlocks", defaultRequiredBlockIdList);
        initialized = true;
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
    static BooleanConfig limitPlaceSpeedConfig;
    static DoubleConfig maxPlaceSpeedPerTick;
    static BooleanConfig disableOnLeftDownConfig;
    static BooleanConfig disableOnGUIOpened;
    static StringListConfig placeableItemsConfig;
    static StringListConfig passableBlocksConfig;
    static BooleanConfig transparentAsPassableConfig;
    static BooleanConfig notOpaqueAsPassableConfig;
    static StringListConfig requiredBlocksConfig;
    private static boolean initialized = false;
    @NotNull
    private static HashSet<Item> itemSetFromIdList(@Nullable List<String> list){
        HashSet<Item> ret = new HashSet<>();
        if(list == null) return ret;
        for(String s : list)
            ret.add(Registries.ITEM.get(Identifier.of(s)));
        return ret;
    }
    @NotNull
    private static HashSet<Block> blockSetFromIdList(@Nullable List<String> list){
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
            switchPlaceMode();
            return true;
        }
    }
    private static class LimitPlaceSpeedCallback implements IValueChangeCallback<ConfigBoolean> {
        @Override
        public void onValueChanged(ConfigBoolean config) {
            maxPlaceSpeedPerTick.enabled = config.getBooleanValue();
            LPCTools.config.showPage();
        }
    }
}
