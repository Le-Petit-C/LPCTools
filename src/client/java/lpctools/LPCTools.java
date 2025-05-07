package lpctools;

import lpctools.debugs.DebugConfigs;
import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.tools.ToolConfigs;
import lpctools.slightTools.SlightToolConfigs;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCTools implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LPCTools");
	public static LPCConfigPage config;

    @Override public void onInitializeClient() {
		if(config != null) return;
		LOGGER.info("Initializing");
		config = new LPCConfigPage(new Reference("LPCTools"));
		GenericConfigs.init(config);
		ToolConfigs.init(config);
		SlightToolConfigs.init(config);
		DebugConfigs.init(config);
		LOGGER.info("Initialized");
	}
}