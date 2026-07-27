package lpctools.tools.liquidCleaner;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockItemListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolUtils;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.liquidCleaner.LiquidCleanerData.*;

public class LiquidCleaner {
    public static final BooleanHotkeyThirdListConfig LCConfig = ToolUtils.configBuilder("LC").withToolRunner(LiquidCleanerRunner::new).build();
    static {listStack.push(LCConfig);}
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final BooleanConfig disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
    public static final BooleanConfig offhandFillingConfig = addBooleanConfig("offhandFilling", false);
    public static final BlockItemListConfig cleaningBlocks = addBlockItemListConfig("cleaningBlocks", defaultCleaningBlocks);
    public static final BooleanConfig ignoreDownwardTest = addBooleanConfig("ignoreDownwardTest", false);
    public static final RangeLimitConfig limitCleaningRange = addRangeLimitConfig();
    public static final BooleanConfig expandRange = addBooleanConfig(limitCleaningRange, "expandRange", false);
    public static final BooleanConfig liquidSourceOnly = addBooleanConfig("liquidSourceOnly", false);
    static {listStack.pop();}
    public static void disableTool(@Nullable String reasonKey) {
        LCConfig.setBooleanValue(false);
        displayDisableReason(LCConfig, reasonKey);
    }
}
