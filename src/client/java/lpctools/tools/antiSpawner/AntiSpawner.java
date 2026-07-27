package lpctools.tools.antiSpawner;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockItemListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.setLPCToolsToggleText;
import static lpctools.tools.antiSpawner.AntiSpawnerData.*;

public class AntiSpawner {
    public static final BooleanHotkeyThirdListConfig ASConfig = ToolUtils.configBuilder("AS").withToolRunner(AntiSpawnerRunner::new).build();
    static {listStack.push(ASConfig);}
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final BlockItemListConfig placeableItems = addBlockItemListConfig("placeableItems", defaultPlaceableItems);
    public static final RangeLimitConfig rangeLimitConfig = addRangeLimitConfig();
}
