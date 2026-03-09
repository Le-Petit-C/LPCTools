package lpctools.mixin.client.accessors;

import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.util.LayerRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LayerRange.class, remap = false)
public interface LayerRangeAccessor {
    @Accessor("refresher") IRangeChangeListener getRefresher();
}
