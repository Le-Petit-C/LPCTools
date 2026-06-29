package lpctools.mixin.client.tools.autoReconnect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tools.autoReconnect.AutoReconnect.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    void onSetScreen(Screen screen, CallbackInfo ci){
        if(screen == null){
            cancelReconnect();
            resetAttemptTimes();
        }
    }
}
