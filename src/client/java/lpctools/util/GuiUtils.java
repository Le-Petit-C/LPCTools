package lpctools.util;

import net.minecraft.client.MinecraftClient;

public class GuiUtils {
    public static boolean isInTextOrGui(){
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen != null && /*client.getOverlay() == null &&*/ client.player != null;
    }
}
