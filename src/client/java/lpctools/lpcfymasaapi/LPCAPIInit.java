package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import lpctools.lpcfymasaapi.gl.Initializer;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LPCAPIInit implements ClientModInitializer, IRenderer {
    public static final Logger LOGGER = LogManager.getLogger("LPCfyMASAAPI");
    static boolean MASAInitialized = false;
    @Override public void onInitializeClient() {
        Registry.init();
        Initializer.init();
        InitializationHandler.getInstance().registerInitializationHandler(this::afterInit);
    }

    private void afterInit() {
        MASAInitialized = true;
        LPCConfigPage.staticAfterInit();
    }
}
