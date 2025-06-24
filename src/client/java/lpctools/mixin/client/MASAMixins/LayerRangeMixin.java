package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.util.LayerRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LayerRange.class, remap = false)
public interface LayerRangeMixin {
    @Accessor("refresher") IRangeChangeListener getRefresher();
}
