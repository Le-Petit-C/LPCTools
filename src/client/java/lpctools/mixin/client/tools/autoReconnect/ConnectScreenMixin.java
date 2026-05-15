package lpctools.mixin.client.tools.autoReconnect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tools.autoReconnect.AutoReconnectData.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;ZLnet/minecraft/client/multiplayer/TransferState;)V",
    at = @At("HEAD"))
    private static void onConnect(Screen screen, Minecraft client, ServerAddress address, ServerData info, boolean quickPlay, TransferState cookieStorage, CallbackInfo ci){
        lastServer = new lpctools.tools.autoReconnect.AutoReconnectData.ServerData(client, address, info, quickPlay, cookieStorage);
    }
}
