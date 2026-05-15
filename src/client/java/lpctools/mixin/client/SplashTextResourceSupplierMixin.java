package lpctools.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(SplashManager.class)
public class SplashTextResourceSupplierMixin {
    @Unique private static final Identifier RESOURCE_ID = Identifier.fromNamespaceAndPath("lpctools", "texts/splashes.txt");
    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
    at = @At("RETURN"), cancellable = true)
    void injectPrepare(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir){
		try (BufferedReader bufferedReader = resourceManager.openAsReader(RESOURCE_ID)) {
			List<Component> extra;
			if (bufferedReader == null) return;
			extra = bufferedReader.lines().map(s -> (Component) Component.literal(s.trim())).toList();
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
