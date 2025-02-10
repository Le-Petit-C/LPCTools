package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.fabricmc.api.ClientModInitializer;

public class LPCAPIInit implements ClientModInitializer {
    static boolean MASAInitialized = false;
    @Override
    public void onInitializeClient() {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
    }

    private static class InitHandler implements IInitializationHandler {
        @Override
        public void registerModHandlers() {
            MASAInitialized = true;
            LPCConfigPage.staticAfterInit();
        }
    }
}
