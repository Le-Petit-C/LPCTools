package lpctools.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
    @Unique private static final Identifier RESOURCE_ID = Identifier.of("lpctools", "texts/splashes.txt");
    @Inject(method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
    at = @At("RETURN"), cancellable = true)
    void injectPrepare(CallbackInfoReturnable<List<String>> cir){
        try {
            List<String> extra;
            BufferedReader bufferedReader = MinecraftClient.getInstance().getResourceManager().openAsReader(RESOURCE_ID);
            if(bufferedReader == null) return;
            try {
                extra = bufferedReader.lines().map(String::trim).toList();
                if(cir.getReturnValue() != null)
                    cir.getReturnValue().addAll(extra);
                else cir.setReturnValue(extra);
            } catch (Throwable e1) {
                try {
                    bufferedReader.close();
                } catch (Throwable e2) {
                    e1.addSuppressed(e2);
                }
                throw e1;
            }
            bufferedReader.close();
        } catch (IOException ignored) {}
    }
}
