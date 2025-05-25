package lpctools.mixin.client.MASAMixins.MuteMASAConfigMinMaxMixin;

import fi.dy.masa.malilib.config.options.ConfigInteger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import static lpctools.mixinInterfaces.MASAMixins.MuteMASAConfigMinMaxMixin.*;

@Mixin(ConfigInteger.class)
public class ConfigIntegerMixin implements MuteMASAConfigMinMaxInteger {
    @Mutable @Final @Shadow(remap = false) protected int minValue;
    @Mutable @Final @Shadow(remap = false) protected int maxValue;
    @Shadow(remap = false) protected int value;
    @Override public int lPCTools$setMin(int value) {
        if(this.value < value) this.value = value;
        int lastValue = minValue;
        minValue = value;
        return lastValue;
    }
    @Override public int lPCTools$setMax(int value) {
        if(this.value > value) this.value = value;
        int lastValue = maxValue;
        maxValue = value;
        return lastValue;
    }
}
