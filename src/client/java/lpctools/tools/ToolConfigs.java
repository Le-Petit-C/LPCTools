package lpctools.tools;

import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.tools.autoGrindstone.AutoGrindstone;
import lpctools.tools.antiSpawner.AntiSpawner;
import lpctools.tools.canSpawnDisplay.CanSpawnDisplay;
import lpctools.tools.slightXRay.SlightXRay;
import lpctools.tools.fillingAssistant.FillingAssistant;
import lpctools.tools.liquidCleaner.LiquidCleaner;

public class ToolConfigs {
    public static final LPCConfigList toolConfigs = new LPCConfigList(LPCTools.page, "tools");
    static {
        toolConfigs.addConfig(FillingAssistant.FAConfig);
        toolConfigs.addConfig(LiquidCleaner.LCConfig);
        toolConfigs.addConfig(SlightXRay.SXConfig);
        toolConfigs.addConfig(AutoGrindstone.AGConfig);
        toolConfigs.addConfig(AntiSpawner.ASConfig);
        toolConfigs.addConfig(CanSpawnDisplay.CSConfig);
    }
}
