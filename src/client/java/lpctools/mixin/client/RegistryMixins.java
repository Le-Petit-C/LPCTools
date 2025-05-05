package lpctools.mixin.client;

import lpctools.lpcfymasaapi.Registry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RegistryMixins {
    @Mixin(MinecraftClient.class)
    public static class afterClientWorldChangeEvent {
        @Inject(method = "setWorld", at = @At("TAIL"))
        private void afterClientWorldChange(ClientWorld world, CallbackInfo ci) {
            if (world != null) {
                MinecraftClient client = (MinecraftClient) (Object) this;
                Registry.runAfterClientWorldChange(client, world);
            }
        }
    }
}
