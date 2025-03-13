package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCAPIInit implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("LPCfyMASAAPI");
    static boolean MASAInitialized = false;
    @Override public void onInitializeClient() {
        Registry.init();
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
    }

    private static class InitHandler implements IInitializationHandler {
        @Override public void registerModHandlers() {
            MASAInitialized = true;
            LPCConfigPage.staticAfterInit();
        }
    }
}
