package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCAPIInit implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("LPCfyMASAAPI");
    static boolean MASAInitialized = false;
    public static boolean isInTextOrGui(){
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen != null && /*client.getOverlay() == null &&*/ client.player != null;
    }
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
