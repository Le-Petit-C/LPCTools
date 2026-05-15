package lpctools.mixin.client.MASAMixins.MuteMASAConfigMinMaxMixin;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import static lpctools.mixinInterfaces.MASAMixins.MuteMASAConfigMinMaxMixin.*;

@Mixin(ConfigDouble.class)
class ConfigDoubleMixin implements MuteMASAConfigMinMaxDouble {
    @Mutable @Final @Shadow(remap = false) private double minValue;
    @Mutable @Final @Shadow(remap = false) private double maxValue;
    @Shadow(remap = false) private double value;
    @Override public double lPCTools$setMin(double value) {
        if(this.value < value) this.value = value;
        double lastValue = minValue;
        minValue = value;
        return lastValue;
    }
    @Override public double lPCTools$setMax(double value) {
        if(this.value > value) this.value = value;
        double lastValue = maxValue;
        maxValue = value;
        return lastValue;
    }
}
