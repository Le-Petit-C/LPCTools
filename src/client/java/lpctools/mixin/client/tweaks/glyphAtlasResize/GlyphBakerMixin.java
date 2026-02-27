package lpctools.mixin.client.tweaks.glyphAtlasResize;

import net.minecraft.client.font.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import static lpctools.tweaks.GlyphAtlasResize.*;

@Mixin(GlyphBaker.class)
public class GlyphBakerMixin {
	@Shadow @Final private List<GlyphAtlasTexture> glyphAtlases;
	@Inject(method = "bake", at = @At("RETURN"))
	void onBakeReturn(CallbackInfoReturnable<BakedGlyphImpl> cir) { scheduleRefresh(); }
}
