package lpctools.mixin.client;

import net.minecraft.client.font.GlyphAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlyphAtlasTexture.class)
public interface GlyphAtlasTextureAccessor {
	@Accessor("SLOT_LENGTH") static void setSlotLength(int length){}
	@Accessor("SLOT_LENGTH") static int getSlotLength(){return 0;}
}
