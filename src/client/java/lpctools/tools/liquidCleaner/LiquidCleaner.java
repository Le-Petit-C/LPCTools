package lpctools.tools.liquidCleaner;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.tools.ToolConfigs;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class LiquidCleaner {
    public static void init(ThirdListConfig LCConfig){
        liquidCleaner = LCConfig.addBooleanHotkeyConfig("liquidCleaner", false, null, ()->valueChangeCallback(liquidCleaner.getAsBoolean()));
        liquidCleaner.getKeybind().setCallback(new KeyCallbackToggleBoolean(liquidCleaner));
        limitInteractSpeedConfig = LCConfig.addThirdListConfig("limitInteractSpeed", false);
        maxBlockPerTickConfig = limitInteractSpeedConfig.addDoubleConfig("maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = LCConfig.addDoubleConfig("reachDistance", 4.5, 0, 5);
        disableOnGUIOpened = LCConfig.addBooleanConfig("disableOnGUIOpened", false);
        offhandFillingConfig = LCConfig.addBooleanConfig("offhandFilling", false);
        blockBlackListConfig = LCConfig.addStringListConfig("blockBlackList", ImmutableList.of(), LiquidCleaner::onBlacklistRefresh);
        ignoreDownwardTest = LCConfig.addBooleanConfig("ignoreDownwardTest", false);
        limitCleaningRange = LCConfig.addRangeLimitConfig(false, "LC");
        expandRange = limitCleaningRange.addBooleanConfig("expandRange", false);
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enableTool(){
        if(isEnabled()) return;
        ToolConfigs.displayEnableMessage(liquidCleaner);
        onEndTick = new OnEndTick();
        Registry.registerEndClientTickCallback(onEndTick);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!isEnabled()) return;
        Registry.unregisterEndClientTickCallback(onEndTick);
        onEndTick = null;
        ToolConfigs.displayDisableReason(liquidCleaner, reasonKey);
    }

    public static BooleanHotkeyConfig liquidCleaner;
    public static ThirdListConfig limitInteractSpeedConfig;
    public static DoubleConfig maxBlockPerTickConfig;
    public static DoubleConfig reachDistanceConfig;
    public static BooleanConfig disableOnGUIOpened;
    public static BooleanConfig offhandFillingConfig;
    public static StringListConfig blockBlackListConfig;
    public static BooleanConfig ignoreDownwardTest;
    public static RangeLimitConfig limitCleaningRange;
    public static BooleanConfig expandRange;
    @Nullable static OnEndTick onEndTick;
    @NotNull static HashSet<Block> blacklistBlocks = new HashSet<>();
    @NotNull static HashSet<Item> blacklistItems = new HashSet<>();

    private static void valueChangeCallback(boolean newValue) {
        if(newValue) enableTool();
        else disableTool(null);
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
