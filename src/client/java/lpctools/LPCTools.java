package lpctools;

import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.tools.ToolConfigs;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCTools implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LPCTools");
	public static LPCConfigPage config;
	@Override public void onInitializeClient() {
		LOGGER.info("Initializing");
		if(config != null) return;
		config = new LPCConfigPage(new Reference("LPCTools"));
		GenericConfigs.init(config);
		ToolConfigs.init(config);
	}
}