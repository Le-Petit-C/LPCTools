package lpctools.tools.autoReconnect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;

public class AutoReconnectData {
    public record ServerData(MinecraftClient mc, ServerAddress address, ServerInfo info, boolean quickPlay, @Nullable CookieStorage cookieStorage){}
    public static @Nullable ServerData lastServer;
    static @Nullable Timer reconnectTask;
    static int attemptTimes = 0;
}
