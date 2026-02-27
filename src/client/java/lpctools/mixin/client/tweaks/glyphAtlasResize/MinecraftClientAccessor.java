package lpctools.mixin.client.tweaks.glyphAtlasResize;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
	@Invoker void invokeOnFontOptionsChanged();
	@Accessor FontManager getFontManager();
}
