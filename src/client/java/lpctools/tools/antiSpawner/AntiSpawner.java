package lpctools.tools.antiSpawner;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.tools.antiSpawner.AntiSpawnerData.*;

public class AntiSpawner {
    public static final BooleanHotkeyThirdListConfig ASConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "AS", AntiSpawner::callback);
    static {listStack.push(ASConfig);}
    static {setLPCToolsToggleText(ASConfig);}
    public static final LimitOperationSpeedConfig limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final String2ObjectListConfig.BlockItemListConfig placeableItems = addBlockItemListConfig("placeableItems", defaultPlaceableItems);
    public static final RangeLimitConfig rangeLimitConfig = addRangeLimitConfig();
    public static void start(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.register(runner);}
    public static void stop(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.unregister(runner);}
    private static void callback(){
        if(ASConfig.getBooleanValue()) start();
        else stop();
    }
}
