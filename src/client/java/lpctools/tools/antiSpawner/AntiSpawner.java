package lpctools.tools.antiSpawner;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.tools.antiSpawner.AntiSpawnerData.*;

public class AntiSpawner {
    public static final ThirdListConfig ASConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "AS", false);
    static {listStack.push(ASConfig);}
    public static final AntiSpawnerSwitch antiSpawnerConfig = addConfig(new AntiSpawnerSwitch());
    static {setLPCToolsToggleText(antiSpawnerConfig);}
    public static final LimitOperationSpeedConfig limitOperationSpeedConfig = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final ObjectListConfig.BlockItemListConfig placeableItems = addBlockItemListConfig("placeableItems", defaultPlaceableItems);
    public static final RangeLimitConfig rangeLimitConfig = addRangeLimitConfig(false);
    public static class AntiSpawnerSwitch extends BooleanHotkeyConfig{
        public AntiSpawnerSwitch() {super(ASConfig, "antiSpawner", false, null);}
        @Override public void onValueChanged() {
            super.onValueChanged();
            if(getBooleanValue()) start();
            else stop();
        }
    }
    public static void start(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.register(runner);}
    public static void stop(){lpctools.lpcfymasaapi.Registries.END_CLIENT_TICK.unregister(runner);}
}
