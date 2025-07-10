package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
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
    static int calculateDisplayLength(String str) {
        int length = 0;
        for (char c : str.toCharArray()) {
            // 检查字符是否是 ASCII 打印字符（半角字符）
            if (c >= 0x20 && c <= 0x7E) {
                length += 1;
            } else {
                length += 2;
            }
        }
        return length;
    }
    static int calculateAndAdjustDisplayLength(String str){
        return calculateDisplayLength(str) * 6 + 8;
    }
}
