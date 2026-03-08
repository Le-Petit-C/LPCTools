package lpctools.mixin.client.tools;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;

@Mixin(Screen.class)
public class FurnaceMaintainer {
    @Inject(method = "renderWithTooltip", at = @At("HEAD"), cancellable = true)
    void mixinScreenRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci){
        if(beforeScreenRendered((Screen)(Object)this)) ci.cancel();
    }
}
