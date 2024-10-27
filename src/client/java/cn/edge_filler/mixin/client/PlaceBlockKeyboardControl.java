package cn.edge_filler.mixin.client;

import cn.edge_filler.Data;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//import java.lang.annotation.Inherited;

@Mixin(Keyboard.class)
public class PlaceBlockKeyboardControl {
    @Shadow @Final private MinecraftClient client;

    @Unique
    boolean isInTextOrGui(){
        Screen screen = this.client.currentScreen;
        return screen != null && this.client.getOverlay() == null;
    }

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci){
        if (window != this.client.getWindow().getHandle())
            return;
        if (isInTextOrGui())
            return;
        if(key == 86 && action == 1)
            Data.switchPlaceMode();
    }
}
