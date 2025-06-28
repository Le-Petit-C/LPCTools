package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.util.LayerRange;
import lpctools.mixinInterfaces.MASAMixins.MuteLayerRangeRefresherMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LayerRange.class, remap = false)
public class MuteLayerRangeRefresher implements MuteLayerRangeRefresherMixin {
    @Shadow @Final @Mutable
    protected IRangeChangeListener refresher;
    @Override
    public void lPCTools$setRefresher(IRangeChangeListener listener) {
        refresher = listener;
    }
}
