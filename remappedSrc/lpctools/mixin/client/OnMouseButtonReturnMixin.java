package lpctools.mixin.client;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class OnMouseButtonReturnMixin {
    @Shadow @Final private Minecraft minecraft;
    @Inject(method = "onPress", at = @At("RETURN"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci){
        if(window != this.minecraft.getWindow().getWindow()) return;
        if(!GuiUtils.isInTextOrGui()) Registries.IN_GAME_END_MOUSE.runner().onInGameEndMouse(button, action, mods);
    }
}
