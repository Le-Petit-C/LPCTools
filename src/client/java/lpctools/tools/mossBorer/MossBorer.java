package lpctools.tools.mossBorer;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class MossBorer {
    public static final BooleanHotkeyThirdListConfig MBConfig = ToolUtils.configBuilder("MB").withToolRunner(MossBorerRunner::new).build();
    static {ToolUtils.setLPCToolsToggleText(MBConfig);}
    static {listStack.push(MBConfig);}
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    static {listStack.pop();}
}
