package lpctools.tools.liquidCleaner;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;

public class LiquidCleaner {
    public static void init(){
        liquidCleaner = addBooleanHotkeyConfig("liquidCleaner", false, null, ()->valueChangeCallback(liquidCleaner.getAsBoolean()));
        liquidCleaner.getKeybind().setCallback(new KeyCallbackToggleBoolean(liquidCleaner));
        limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
        reachDistanceConfig = addReachDistanceConfig();
        disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
        offhandFillingConfig = addBooleanConfig("offhandFilling", false);
        blockBlackListConfig = addStringListConfig("blockBlackList", ImmutableList.of(), LiquidCleaner::onBlacklistRefresh);
        ignoreDownwardTest = addBooleanConfig("ignoreDownwardTest", false);
        limitCleaningRange = addRangeLimitConfig(false);
        expandRange = addBooleanConfig(limitCleaningRange, "expandRange", false);
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enableTool(){
        if(isEnabled()) return;
        displayEnableMessage(liquidCleaner);
        onEndTick = new OnEndTick();
        lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.register(onEndTick);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!isEnabled()) return;
        lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.unregister(onEndTick);
        onEndTick = null;
        displayDisableReason(liquidCleaner, reasonKey);
    }

    public static BooleanHotkeyConfig liquidCleaner;
    public static LimitOperationSpeedConfig limitOperationSpeedConfig;
    public static ReachDistanceConfig reachDistanceConfig;
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
