package lpctools.tools.autoReconnect;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class AutoReconnect {
    public static final BooleanHotkeyThirdListConfig ARConfig = ToolUtils.configBuilder("AR").withToolRunner(AutoReconnectRunner::new).build();
    static {listStack.push(ARConfig);}
    public static final DoubleConfig firstAttemptDelay = addDoubleConfig("firstAttemptDelay", 1, 0, 60);
    public static final DoubleConfig delayLinearFactor = addDoubleConfig("delayLinearFactor", 0, 0, 60);
    public static final DoubleConfig delayExpFactor = addDoubleConfig("delayExpFactor", 1.6180339887, 1, 10);
    public static final IntegerConfig maxAttemptTimes = addIntegerConfig("maxAttemptTimes", -1, -1, Integer.MAX_VALUE);
    static {listStack.pop();}
}
