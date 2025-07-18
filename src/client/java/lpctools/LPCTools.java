package lpctools;

import lpctools.debugs.DebugConfigs;
import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.scripts.ScriptConfigs;
import lpctools.shader.LPCShaderInitializer;
import lpctools.tools.ToolConfigs;
import lpctools.tweaks.TweakConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCTools {
	public static void init(){}
	public static final Logger LOGGER = LogManager.getLogger("LPCTools");
	static {LOGGER.info("Initializing");}
	static {LPCShaderInitializer.init();}
	public static final LPCConfigPage page = new LPCConfigPage(new Reference("LPCTools"));
	static {
		page.addList(GenericConfigs.generic);
		page.addList(ToolConfigs.toolConfigs);
		page.addList(TweakConfigs.tweaks);
		page.addList(DebugConfigs.debugs);
		page.addList(ScriptConfigs.script);
	}
	static {LOGGER.info("Initialized");}
}