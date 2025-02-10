package lpctools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public class Data {
    public static Boolean shouldplace = false;
    public enum BLOCKTYPE{
        STONE,
        EMPTY,
        OTHERS,
        ERROR
    }
    public static Item mainHandContainedItem;
    public static void switchPlaceMode(){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if(Data.shouldplace) {
            Data.shouldplace = false;
            player.sendMessage(Text.literal("已关闭功能: edge_filler"), false);
        }
        else{
            if(player.getMainHandStack().isEmpty()){
                player.sendMessage(Text.literal("无法开启功能: edge_filler"), false);
                player.sendMessage(Text.literal("主手为空"), false);
                return;
            }
            Data.shouldplace = true;
            player.sendMessage(Text.literal("已开启功能: edge_filler"), false);
            mainHandContainedItem = player.getMainHandStack().getItem();
        }
    }
    public static boolean isInTextOrGui(MinecraftClient client){
        Screen screen = client.currentScreen;
        return screen != null && client.getOverlay() == null;
    }
}
