package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.mixin.client.GlyphAtlasTextureAccessor;
import lpctools.mixin.client.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class GlyphAtlasResize {
	public static final BooleanThirdListConfig glyphAtlasResize = new BooleanThirdListConfig(TweakConfigs.tweaks, "glyphAtlasResize", false, GlyphAtlasResize::refreshGlyph);
	static {listStack.push(glyphAtlasResize);}
	public static final UniqueIntegerConfig glyphAtlasSize = addConfig(new UniqueIntegerConfig(peekConfigList(), "glyphAtlasSize", 256, 256, 65536, GlyphAtlasResize::refreshGlyph));
	static {listStack.pop();}
	
	private static void refreshGlyph() {
		int newLength = glyphAtlasResize.getBooleanValue() ? glyphAtlasSize.getIntegerValue() : 256;
		if(newLength == GlyphAtlasTextureAccessor.getSlotLength()) return;
		GlyphAtlasTextureAccessor.setSlotLength(newLength);
		var client = MinecraftClient.getInstance();
		((MinecraftClientAccessor)client).invokeOnFontOptionsChanged();
	}
}
