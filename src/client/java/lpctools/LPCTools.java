package lpctools;

import lpctools.compat.minihud.MiniHUDMethods;
import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.tools.ToolConfigs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LPCTools implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LPCTools");
	public static LPCConfigPage config;
	@Nullable private static MiniHUDMethods miniHUDMethods = null;
	@Override public void onInitializeClient() {
		if(config != null) return;
		LOGGER.info("Initializing");
		if(FabricLoader.getInstance().isModLoaded("minihud"))
			miniHUDMethods = new MiniHUDMethods();
		config = new LPCConfigPage(new Reference("LPCTools"));
		GenericConfigs.init(config);
		ToolConfigs.init(config);
		LOGGER.info("Initialized");
	}
	@Nullable public static MiniHUDMethods getMiniHUDMethods(){
		return miniHUDMethods;
	}
}