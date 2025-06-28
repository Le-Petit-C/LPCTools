package lpctools.mixin.client.events;

import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class AfterClientWorldChange {
    @Inject(method = "setWorld", at = @At("TAIL"))
    private void afterClientWorldChange(ClientWorld world, CallbackInfo ci) {
        if (world != null) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            Registries.AFTER_CLIENT_WORLD_CHANGE.run().afterWorldChange(client, world);
        }
    }
}
