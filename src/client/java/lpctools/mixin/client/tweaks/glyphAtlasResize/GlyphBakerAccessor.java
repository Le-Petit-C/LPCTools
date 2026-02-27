package lpctools.mixin.client.tweaks.glyphAtlasResize;

import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GlyphBaker.class)
public interface GlyphBakerAccessor {
	@Accessor List<GlyphAtlasTexture> getGlyphAtlases();
}
