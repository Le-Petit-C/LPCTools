package lpctools.tools;

import fi.dy.masa.malilib.config.IConfigBoolean;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.tools.autoGrindstone.AutoGrindstone;
import lpctools.tools.antiSpawner.AntiSpawner;
import lpctools.tools.autoReconnect.AutoReconnect;
import lpctools.tools.breakRestriction.BreakRestriction;
import lpctools.tools.canSpawnDisplay.CanSpawnDisplay;
import lpctools.tools.entityHighlight.EntityHighlight;
import lpctools.tools.furnaceMaintainer.FurnaceMaintainer;
import lpctools.tools.mossBorer.MossBorer;
import lpctools.tools.slightXRay.SlightXRay;
import lpctools.tools.fillingAssistant.FillingAssistant;
import lpctools.tools.liquidCleaner.LiquidCleaner;
import lpctools.tools.tilingTool.TilingTool;

public class ToolConfigs {
    public static final LPCConfigList toolConfigs = new LPCConfigList(LPCTools.page, "tools");
    public static final ButtonConfig closeAll = new ButtonConfig(toolConfigs, "closeAll", (button, mouseButton)->
        toolConfigs.getConfigs().forEach(config->{
		if(config instanceof IConfigBoolean configBoolean)
			configBoolean.setBooleanValue(false);
	}));
    static {
        toolConfigs.addConfigs(
            closeAll,
            FillingAssistant.FAConfig,
            LiquidCleaner.LCConfig,
            SlightXRay.SXConfig,
            AutoGrindstone.AGConfig,
            AntiSpawner.ASConfig,
            CanSpawnDisplay.CSConfig,
            BreakRestriction.BRConfig,
            TilingTool.TTConfig,
            FurnaceMaintainer.FMConfig,
            MossBorer.MBConfig,
            AutoReconnect.ARConfig,
            EntityHighlight.EHConfig
        );
    }
}
