package lpctools.mixin.client;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class OnMouseButtonReturnMixin {
    @Shadow @Final private Minecraft minecraft;
    @Inject(method = "onButton", at = @At("RETURN"))
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci){
        if(window != this.minecraft.getWindow().handle()) return;
        if(!GuiUtils.isInTextOrGui()) Registries.IN_GAME_END_MOUSE.runner().onInGameEndMouse(input, action);
    }
}
