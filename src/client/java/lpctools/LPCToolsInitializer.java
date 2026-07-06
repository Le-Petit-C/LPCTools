package lpctools;

import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import lpctools.lpcfymasaapi.Registries;
import net.fabricmc.api.ClientModInitializer;

public class LPCToolsInitializer implements ClientModInitializer, IInitializationHandler {
    @Override public void onInitializeClient() {
        // InitializationHandler.getInstance().registerInitializationHandler(this);
        LPCTools.init();
        Registries.CLIENT_CONTAINER_CONTENT_INITIALIZED.register(menu ->
            LPCTools.LOGGER.info("Content for container menu {} initialized", menu));
    }
    
    @Override public void registerModHandlers() {
        // LPCTools.init();
    }
}
