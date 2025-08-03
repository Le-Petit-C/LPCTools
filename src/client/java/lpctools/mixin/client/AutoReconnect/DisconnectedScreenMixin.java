package lpctools.mixin.client.AutoReconnect;

import lpctools.tools.autoReconnect.AutoReconnect;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {
    @Inject(method = "init", at = @At("RETURN"))
    void onDisconnect(CallbackInfo ci){
        AutoReconnect.disconnected();
    }
}
