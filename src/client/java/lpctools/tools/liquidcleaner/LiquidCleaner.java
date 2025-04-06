package lpctools.tools.liquidcleaner;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class LiquidCleaner {
    public static void init(ThirdListConfig LCConfig){
        hotkeyConfig = LCConfig.addHotkeyConfig("LC_Hotkey", "", LiquidCleaner::hotkeyCallback);
        limitInteractSpeedConfig = LCConfig.addThirdListConfig("LC_limitInteractSpeed", false);
        maxBlockPerTickConfig = limitInteractSpeedConfig.addDoubleConfig("LC_maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = LCConfig.addDoubleConfig("LC_reachDistance", 4.5, 0, 5);
        disableOnGUIOpened = LCConfig.addBooleanConfig("LC_disableOnGUIOpened", false);
        offhandFillingConfig = LCConfig.addBooleanConfig("LC_OffhandFilling", false);
        blockBlackListConfig = LCConfig.addStringListConfig("LC_BlockBlackList", ImmutableList.of(), LiquidCleaner::onBlacklistRefresh);
        limitCleaningRange = LCConfig.addThirdListConfig("LC_LimitCleaningRange", false);
        rangeNamePrefix = limitCleaningRange.addStringConfig("LC_RangeNamePrefix");
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enableTool(){
        if(isEnabled()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.LC_enableNotification")), true);
        onEndTick = new OnEndTick();
        Registry.registerEndClientTickCallback(onEndTick);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!isEnabled()) return;
        Registry.unregisterEndClientTickCallback(onEndTick);
        onEndTick = null;
        ToolConfigs.displayDisableReason("LC_disableNotification", reasonKey);
    }

    public static HotkeyConfig hotkeyConfig;
    public static ThirdListConfig limitInteractSpeedConfig;
    public static DoubleConfig maxBlockPerTickConfig;
    public static DoubleConfig reachDistanceConfig;
    public static BooleanConfig disableOnGUIOpened;
    public static BooleanConfig offhandFillingConfig;
    public static StringListConfig blockBlackListConfig;
    public static ThirdListConfig limitCleaningRange;
    public static StringConfig rangeNamePrefix;
    @Nullable static OnEndTick onEndTick;
    @NotNull static HashSet<Block> blacklistBlocks = new HashSet<>();
    @NotNull static HashSet<Item> blacklistItems = new HashSet<>();

    private static boolean hotkeyCallback(KeyAction action, IKeybind key) {
        if(isEnabled()) disableTool(null);
        else enableTool();
        return true;
    }
    private static void onBlacklistRefresh(){
        blacklistBlocks.clear();
        blacklistItems.clear();
        List<String> blacklist = blockBlackListConfig.getStrings();
        for(String str : blacklist){
            Block block = Registries.BLOCK.get(Identifier.of(str));
            blacklistBlocks.add(block);
            blacklistItems.add(block.asItem());
        }
    }
}
