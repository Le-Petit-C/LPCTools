package lpctools.mixin.client;

import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
    @Unique private static final Identifier RESOURCE_ID = Identifier.of("lpctools", "texts/splashes.txt");
    @Inject(method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
    at = @At("RETURN"), cancellable = true)
    void injectPrepare(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<List<String>> cir){
		try (BufferedReader bufferedReader = resourceManager.openAsReader(RESOURCE_ID)) {
			List<String> extra;
			if (bufferedReader == null) return;
			extra = bufferedReader.lines().map(String::trim).toList();
			if (cir.getReturnValue() != null) {
				try {
					cir.getReturnValue().addAll(extra);
				} catch (UnsupportedOperationException ignored) {
					var arrayListTexts = new ArrayList<>(cir.getReturnValue());
					arrayListTexts.addAll(extra);
					cir.setReturnValue(arrayListTexts);
				}
			}
			else cir.setReturnValue(extra);
		}
        catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
    }
}
