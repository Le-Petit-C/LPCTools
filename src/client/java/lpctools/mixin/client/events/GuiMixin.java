package lpctools.mixin.client.events;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.Registries.BEFORE_SCREEN_CHANGE;
import static lpctools.lpcfymasaapi.Registries.ON_SCREEN_CHANGED;

import net.minecraft.client.gui.screens.Screen;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    void injectSetScreenHead(Screen screen, CallbackInfo ci){
        if(BEFORE_SCREEN_CHANGE.runner().beforeScreenChange(screen))
            ci.cancel();
    }
    @Inject(method = "setScreen", at = @At("TAIL"))
    void injectSetScreenTail(Screen screen, CallbackInfo ci){
        ON_SCREEN_CHANGED.runner().run();
    }
}
