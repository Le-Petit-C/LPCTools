package lpctools;

import lpctools.Generic.GenericOptions;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Reference;
import net.fabricmc.api.ClientModInitializer;

public class LPCTools implements ClientModInitializer {
	LPCConfigPage config;
	@Override
	public void onInitializeClient() {
		config = new LPCConfigPage(new Reference("LPCTools"));
		GenericOptions.init(config);
	}
}