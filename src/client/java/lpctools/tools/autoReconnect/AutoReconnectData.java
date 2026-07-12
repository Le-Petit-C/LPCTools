package lpctools.tools.autoReconnect;

import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class AutoReconnectData {
    public record ServerData(Minecraft mc, ServerAddress address, net.minecraft.client.multiplayer.ServerData info, boolean quickPlay, @Nullable TransferState cookieStorage){}
    public static @Nullable ServerData lastServer;
    static @Nullable Timer reconnectTask;
    static int attemptTimes = 0;
}
