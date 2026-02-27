package lpctools.mixin.client.tweaks.glyphAtlasResize;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontStorage.class)
public interface FontStorageAccessor {
	@Accessor GlyphBaker getGlyphBaker();
}
