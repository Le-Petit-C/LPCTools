package lpctools.mixin.client.tweaks.glyphAtlasResize;

import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.TextRenderLayerSet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(GlyphAtlasTexture.class)
public class GlyphAtlasTextureMixin {
	@Shadow @Final @Mutable private static int SLOT_LENGTH;
	@Unique int slotLength;
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/AbstractTexture;<init>()V", shift = At.Shift.AFTER))
	private void onInit(Supplier<String> nameSupplier, TextRenderLayerSet textRenderLayers, boolean hasColor, CallbackInfo ci) {
		slotLength = SLOT_LENGTH;
	}
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 256))
	int modifyAtlasSize(int original) { return slotLength; }
	@ModifyConstant(method = "bake(Lnet/minecraft/client/font/GlyphMetrics;Lnet/minecraft/client/font/UploadableGlyph;)Lnet/minecraft/client/font/BakedGlyphImpl;", constant = @Constant(floatValue = 256.0f))
	float modifyBakeAtlasSize(float original) { return slotLength; }
}
