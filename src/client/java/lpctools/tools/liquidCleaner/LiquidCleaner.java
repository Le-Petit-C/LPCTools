package lpctools.tools.liquidCleaner;

import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.liquidCleaner.LiquidCleanerData.*;

public class LiquidCleaner {
    public static final BooleanHotkeyThirdListConfig LCConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "LC", LiquidCleaner::switchCallback);
    static {LCConfig.getKeybind().setCallback(new KeyCallbackToggleBoolean(LCConfig));}
    static {listStack.push(LCConfig);}
    public static final LimitOperationSpeedConfig limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final BooleanConfig disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
    public static final BooleanConfig offhandFillingConfig = addBooleanConfig("offhandFilling", false);
    public static final ObjectListConfig.BlockListConfig cleaningBlocks = addBlockListConfig("cleaningBlocks", defaultCleaningBlocks);
    public static final BooleanConfig ignoreDownwardTest = addBooleanConfig("ignoreDownwardTest", false);
    public static final RangeLimitConfig limitCleaningRange = addRangeLimitConfig();
    public static final BooleanConfig expandRange = addBooleanConfig(limitCleaningRange, "expandRange", false);
    static {listStack.pop();}
    private static void switchCallback() {
        if(LCConfig.getBooleanValue()) enableTool();
        else disableTool(null);
    }
    public static boolean isEnabled(){return runner != null;}
    public static void enableTool(){
        if(isEnabled()) return;
        displayEnableMessage(LCConfig);
        runner = new LiquidCleanerRunner();
        lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.register(runner);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!isEnabled()) return;
        lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.unregister(runner);
        runner = null;
        displayDisableReason(LCConfig, reasonKey);
    }
}
