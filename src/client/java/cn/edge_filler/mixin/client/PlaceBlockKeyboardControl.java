package cn.edge_filler.mixin.client;

import cn.edge_filler.Data;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class PlaceBlockKeyboardControl {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci){
        if (window != this.client.getWindow().getHandle()) return;
        if (Data.isInTextOrGui(client)) return;
        if(key == '`' && action == 1)
            Data.switchPlaceMode();
    }
}
