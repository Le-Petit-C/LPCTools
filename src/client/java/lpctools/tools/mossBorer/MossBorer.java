package lpctools.tools.mossBorer;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.mossBorer.MossBorerData.*;

public class MossBorer {
    public static final BooleanHotkeyThirdListConfig MBConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "MB", MossBorer::switchCallback);
    static {ToolUtils.setLPCToolsToggleText(MBConfig);}
    static {listStack.push(MBConfig);}
    public static final LimitOperationSpeedConfig operationSpeed = addLimitOperationSpeedConfig(false, 1);
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    static {listStack.pop();}
    private static void switchCallback(){
        if(MBConfig.getBooleanValue()){
            if(runner == null) runner = new MossBorerRunner();
        }
        else {
            if(runner != null){
                runner.close();
                runner = null;
            }
        }
    }
}
