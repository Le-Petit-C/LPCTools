package lpctools.compact;

import lpctools.compact.litematica.LitematicaMethods;
import lpctools.compact.minihud.MiniHUDMethods;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

public class CompactMain {
    public static @Nullable MiniHUDMethods getMinihudInstance() {tryInit();return minihudInstance;}
    public static @Nullable LitematicaMethods getLitematicaInstance() {tryInit();return litematicaInstance;}
    private static @Nullable MiniHUDMethods minihudInstance = null;
    private static @Nullable LitematicaMethods litematicaInstance = null;
    private static boolean isInitialized = false;
    private static void tryInit(){
        if(isInitialized) return;
        isInitialized = true;
        if(FabricLoader.getInstance().isModLoaded("minihud")) minihudInstance = new MiniHUDMethods();
        if(FabricLoader.getInstance().isModLoaded("litematica")) litematicaInstance = new LitematicaMethods();
    }

}
