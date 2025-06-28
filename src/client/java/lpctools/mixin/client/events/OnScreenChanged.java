package lpctools.mixin.client.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.Registries.ON_SCREEN_CHANGED;

@Mixin(MinecraftClient.class)
public class OnScreenChanged {
    @Inject(method = "setScreen", at = @At("TAIL"))
    void setScreenMixin(Screen screen, CallbackInfo ci){
        ON_SCREEN_CHANGED.run().onScreenChanged(screen);
    }
}
