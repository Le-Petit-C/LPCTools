package lpctools.mixinInterfaces.MASAMixins;

import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.util.LayerRange;

public interface MuteLayerRangeRefresherMixin {
    void lPCTools$setRefresher(IRangeChangeListener listener);
    static void setRefresher(LayerRange range, IRangeChangeListener listener){
        ((MuteLayerRangeRefresherMixin)range).lPCTools$setRefresher(listener);
    }
}
