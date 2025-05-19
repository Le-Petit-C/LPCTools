package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;

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
}
