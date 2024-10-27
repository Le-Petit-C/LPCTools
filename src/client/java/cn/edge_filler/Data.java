package cn.edge_filler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Data {
    public static Boolean shouldplace = false;
    public enum BLOCKTYPE{
        STONE,
        EMPTY,
        OTHERS,
        ERROR
    }
    public static void switchPlaceMode(){
        Data.shouldplace = !Data.shouldplace;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("已" + (Data.shouldplace ? "开启" : "关闭") + "功能: edge_filler"), false);
        }
    }
}
