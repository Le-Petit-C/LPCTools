package lpctools;

import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.fabricmc.api.ClientModInitializer;

public class LPCToolsInitializer implements ClientModInitializer, IInitializationHandler {
    @Override public void onInitializeClient() {
        // InitializationHandler.getInstance().registerInitializationHandler(this);
        LPCTools.init();
    }
    
    @Override public void registerModHandlers() {
        // LPCTools.init();
    }
}
