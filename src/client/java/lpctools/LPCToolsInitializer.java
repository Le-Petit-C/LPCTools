package lpctools;

import net.fabricmc.api.ClientModInitializer;

public class LPCToolsInitializer implements ClientModInitializer {
    @Override public void onInitializeClient() {
        LPCTools.init();
    }
}
