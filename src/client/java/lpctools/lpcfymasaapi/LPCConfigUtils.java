package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;

import static lpctools.mixinInterfaces.MASAMixins.MuteMASAConfigMinMaxMixin.*;

public class LPCConfigUtils {
    public static double muteMaxValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMax(value);
    }
    public static double muteMinValue(ConfigDouble config, double value){
        return ((MuteMASAConfigMinMaxDouble)config).lPCTools$setMin(value);
    }
    public static int muteMaxValue(ConfigInteger config, int value){
        return ((MuteMASAConfigMinMaxInteger)config).lPCTools$setMax(value);
    }
    public static int muteMinValue(ConfigInteger config, int value){
        return ((MuteMASAConfigMinMaxInteger)config).lPCTools$setMin(value);
    }
}
