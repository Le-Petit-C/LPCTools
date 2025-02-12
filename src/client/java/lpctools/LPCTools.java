package lpctools;

import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import lpctools.tools.ToolConfigs;
import net.fabricmc.api.ClientModInitializer;

public class LPCTools implements ClientModInitializer {
	public static LPCConfigPage config;
	@Override
	public void onInitializeClient() {
		if(config != null) return;
		config = new LPCConfigPage(new Reference("LPCTools"));
		GenericConfigs.init(config);
		ToolConfigs.init(config);
	}
}