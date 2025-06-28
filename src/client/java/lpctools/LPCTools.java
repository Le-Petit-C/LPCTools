package lpctools;

import lpctools.debugs.DebugConfigs;
import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.shader.LPCShaderInitializer;
import lpctools.tools.ToolConfigs;
import lpctools.slightTools.SlightToolConfigs;
import lpctools.tweaks.TweakConfigs;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class LPCTools implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LPCTools");
	public static LPCConfigPage page;
	public static LPCConfigList generic;
	public static LPCConfigList tools;
	public static LPCConfigList slightTools;
	public static LPCConfigList tweaks;
	public static LPCConfigList debugs;

    @Override public void onInitializeClient() {
		if(page != null) return;
		LOGGER.info("Initializing");
		LPCShaderInitializer.init();
		page = new LPCConfigPage(new Reference("LPCTools"));
		try(ConfigListLayer layer = new ConfigListLayer()){
			layer.set(generic = page.addList("generic"));
			GenericConfigs.init();
			layer.set(tools = page.addList("tools"));
			ToolConfigs.init();
			layer.set(slightTools = page.addList("slightTools"));
			SlightToolConfigs.init();
			layer.set(tweaks = page.addList("tweaks"));
			TweakConfigs.init();
			layer.set(debugs = page.addList("debugs"));
			DebugConfigs.init();
		}
		LOGGER.info("Initialized");
	}
}