package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import net.minecraft.client.font.TextRenderer;
import org.jetbrains.annotations.NotNull;

import static lpctools.mixinInterfaces.MASAMixins.MuteMASAConfigMinMaxMixin.*;

public interface LPCConfigUtils {
    static double muteMaxValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMax(value);
    }
    static double muteMinValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMin(value);
    }
    static int muteMaxValue(ConfigInteger config, int value){
        return ((MuteMASAConfigMinMaxInteger)config).lPCTools$setMax(value);
    }
    static int muteMinValue(ConfigInteger config, int value){
        return ((MuteMASAConfigMinMaxInteger)config).lPCTools$setMin(value);
    }
    static void warnFailedLoadingConfig(ILPCConfigBase configThis, @NotNull JsonElement element){
        LPCAPIInit.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", configThis.getNameKey(), element);
    }
    static int calculateTextButtonWidth(String str, TextRenderer textRenderer, int barHeight){
		return (int)Math.round((textRenderer.getWidth(str) + ((barHeight - textRenderer.fontHeight) * 1.414213562)) / 2) * 2;
    }
}
