package lpctools.mixin.client.AutoReconnect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tools.autoReconnect.AutoReconnect.*;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    void onSetScreen(Screen screen, CallbackInfo ci){
        if(screen == null){
            cancelReconnect();
            resetAttemptTimes();
        }
    }
}
