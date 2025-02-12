package lpctools.tools.fillingassistant;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class Data {
    public static Boolean enabled(){
        return thread != null;
    }
    public enum BLOCKTYPE{
        STONE,
        EMPTY,
        OTHERS,
        ERROR
    }
    public static void enableTool(){
        if(thread != null) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        thread = new Thread(FillingAssistant.getThreadCallback());
        thread.start();
        player.sendMessage(Text.literal("已开启功能: fillingAssistant"), true);
    }
    public static void disableTool(String reason){
        if(thread == null) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        thread = null;
        player.sendMessage(Text.literal("已关闭功能: fillingAssistant" + reason), true);
    }
    public static void switchPlaceMode(){
        if(enabled()) disableTool("");
        else enableTool();
    }
    public static boolean isInTextOrGui(MinecraftClient client){
        Screen screen = client.currentScreen;
        return screen != null && client.getOverlay() == null;
    }
    private static Thread thread;
}
