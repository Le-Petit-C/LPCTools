package lpctools.mixin.client.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.Registries.ON_SCREEN_CHANGED;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public class OnScreenChanged {
    @Inject(method = "setScreen", at = @At("TAIL"))
    void setScreenMixin(Screen screen, CallbackInfo ci){
        ON_SCREEN_CHANGED.runner().onScreenChanged(screen);
    }
}
