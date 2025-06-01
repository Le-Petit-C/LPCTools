package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class MaskLayer implements AutoCloseable{
    private final Constants.EnableMask[] masks;
    private final boolean[] maskValues;
    public MaskLayer(Constants.EnableMask[] masks, boolean[] maskValues){
        assert masks.length == maskValues.length;
        this.masks = new Constants.EnableMask[masks.length];
        this.maskValues = new boolean[maskValues.length];
        for(int a = 0; a < masks.length; ++a){
            this.masks[a] = masks[a];
            this.maskValues[a] = this.masks[a].isEnabled();
            this.masks[a].enable(maskValues[a]);
        }
    }
    public MaskLayer(ArrayList<Object2BooleanMap.Entry<Constants.EnableMask>> masks){
        this.masks = new Constants.EnableMask[masks.size()];
        maskValues = new boolean[masks.size()];
        for(int a = 0; a < masks.size(); ++a){
            this.masks[a] = masks.get(a).getKey();
            maskValues[a] = this.masks[a].isEnabled();
            this.masks[a].enable(masks.get(a).getBooleanValue());
        }
    }
    @Override public void close() {
        for(int a = 0; a < masks.length; ++a)
            masks[a].enable(maskValues[a]);
    }
}
