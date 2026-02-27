package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.mixin.client.tweaks.glyphAtlasResize.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class GlyphAtlasResize {
	public static final BooleanThirdListConfig glyphAtlasResize = new BooleanThirdListConfig(TweakConfigs.tweaks, "glyphAtlasResize", false, GlyphAtlasResize::scheduleRefresh);
	static {listStack.push(glyphAtlasResize);}
	public static final UniqueBooleanConfig autoResize = addConfig(new UniqueBooleanConfig(peekConfigList(), "autoResize", true, GlyphAtlasResize::autoResizeChanged));
	public static final UniqueIntegerConfig glyphAtlasSize = addConfig(new UniqueIntegerConfig(peekConfigList(), "glyphAtlasSize", 256, 256, 65536, GlyphAtlasResize::scheduleRefresh));
	static {listStack.pop();}
	
	private static boolean refreshScheduled = false;
	private static boolean needReset = false;
	
	public static void scheduleRefresh() {
		if(refreshScheduled) return;
		refreshScheduled = true;
		MinecraftClient client = MinecraftClient.getInstance();
		client.send(GlyphAtlasResize::refresh);
	}
	
	private static boolean isStorageBakerSizeOutOfBound(FontStorage fontStorage) {
		return ((GlyphBakerAccessor)((FontStorageAccessor)fontStorage).getGlyphBaker()).getGlyphAtlases().size() > 1;
	}
	
	private static boolean hasGlyphAtlasBakerSizeOutOuBound(FontManager fontManager) {
		if(isStorageBakerSizeOutOfBound(((FontManagerAccessor)fontManager).getMissingStorage())) return true;
		for(var fontStorage : ((FontManagerAccessor)fontManager).getFontStorages().values())
			if(isStorageBakerSizeOutOfBound(fontStorage)) return true;
		return false;
	}
	
	private static void refresh() {
		refreshScheduled = false;
		MinecraftClient client = MinecraftClient.getInstance();
		FontManager fontManager = ((MinecraftClientAccessor)client).getFontManager();
		if(autoResize.getBooleanValue() && hasGlyphAtlasBakerSizeOutOuBound(fontManager))
			glyphAtlasSize.setIntegerValue(glyphAtlasSize.getIntegerValue() * 2);
		else if(needReset) glyphAtlasSize.setIntegerValue(256);
		needReset = false;
		int newLength = glyphAtlasResize.getBooleanValue() ? glyphAtlasSize.getIntegerValue() : 256;
		if(newLength == GlyphAtlasTextureAccessor.getSlotLength()) return;
		GlyphAtlasTextureAccessor.setSlotLength(newLength);
		((MinecraftClientAccessor)client).invokeOnFontOptionsChanged();
		((FontManagerAccessor)fontManager).getAnyFonts().clear();
		((FontManagerAccessor)fontManager).getAdvanceValidatedFonts().clear();
	}
	
	private static void autoResizeChanged() {
		if(autoResize.getBooleanValue()) needReset = true;
		scheduleRefresh();
	}
}
