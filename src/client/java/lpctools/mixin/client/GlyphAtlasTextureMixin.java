package lpctools.mixin.client;

import net.minecraft.client.font.GlyphAtlasTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GlyphAtlasTexture.class)
public class GlyphAtlasTextureMixin {
	@Shadow @Final @Mutable private static int SLOT_LENGTH = 256;
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 256))
	int modifyAtlasSize(int original) { return SLOT_LENGTH; }
	@ModifyConstant(method = "bake(Lnet/minecraft/client/font/GlyphMetrics;Lnet/minecraft/client/font/UploadableGlyph;)Lnet/minecraft/client/font/BakedGlyphImpl;", constant = @Constant(floatValue = 256.0f))
	float modifyBakeAtlasSize(float original) { return SLOT_LENGTH; }
}
