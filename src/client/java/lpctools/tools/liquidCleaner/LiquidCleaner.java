package lpctools.tools.liquidCleaner;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.tools.ToolConfigs;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;

public class LiquidCleaner {
    public static final ThirdListConfig LCConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "LC", false);
    static {listStack.push(LCConfig);}
    public static final BooleanHotkeyConfig liquidCleaner = addBooleanHotkeyConfig("liquidCleaner", false, null, LiquidCleaner::liquidCleanerCallback);
    static {liquidCleaner.getKeybind().setCallback(new KeyCallbackToggleBoolean(liquidCleaner));}
    public static final LimitOperationSpeedConfig limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final BooleanConfig disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
    public static final BooleanConfig offhandFillingConfig = addBooleanConfig("offhandFilling", false);
    public static final ObjectListConfig.BlockListConfig blockBlackListConfig = addBlockListConfig("blockBlackList", ImmutableList.of());
    public static final BooleanConfig ignoreDownwardTest = addBooleanConfig("ignoreDownwardTest", false);
    public static final RangeLimitConfig limitCleaningRange = addRangeLimitConfig(false);
    public static final BooleanConfig expandRange = addBooleanConfig(limitCleaningRange, "expandRange", false);
    static {listStack.pop();}
    private static void liquidCleanerCallback() {
        if(liquidCleaner.getBooleanValue()) enableTool();
        else disableTool(null);
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
    @Nullable static OnEndTick onEndTick;
}
