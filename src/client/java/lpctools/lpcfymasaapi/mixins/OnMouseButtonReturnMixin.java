package lpctools.lpcfymasaapi.mixins;

import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.lpcfymasaapi.Registry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class OnMouseButtonReturnMixin {
    @Shadow @Final private MinecraftClient client;
    @Inject(method = "onMouseButton", at = @At("RETURN"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci){
        if(window != this.client.getWindow().getHandle()) return;
        if(!LPCAPIInit.isInTextOrGui())
            Registry.runInGameEndMouseCallbacks(button, action, mods);
    }
}
