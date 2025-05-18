package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.config.options.ConfigDouble;

import static lpctools.mixinInterfaces.MASAMixins.MuteMASAConfigMinMaxMixin.*;

public class LPCConfigUtils {
    public static double muteMaxValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMax(value);
    }
    public static double muteMinValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMin(value);
    }
}
