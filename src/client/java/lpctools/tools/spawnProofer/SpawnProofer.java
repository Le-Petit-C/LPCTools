package lpctools.tools.spawnProofer;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockItemListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.spawnProofer.SpawnProoferData.*;

public class SpawnProofer {
    public static final BooleanHotkeyThirdListConfig ASConfig = ToolUtils.configBuilder("AS").withToolRunner(SpawnProoferRunner::new).build();
    static {listStack.push(ASConfig);}
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig();
    public static final BlockItemListConfig placeableItems = addBlockItemListConfig("placeableItems", defaultPlaceableItems);
    public static final RangeLimitConfig rangeLimitConfig = addRangeLimitConfig();
}
