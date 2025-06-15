package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;

import java.util.ArrayList;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MaskLayer implements AutoCloseable{
    private final ArrayList<ObjectBooleanImmutablePair<Constants.EnableMask>> masks;
    public MaskLayer(){masks = new ArrayList<>();}
    public MaskLayer enable(Constants.EnableMask mask, boolean value){
        masks.add(new ObjectBooleanImmutablePair<>(mask, mask.isEnabled()));
        mask.enable(value);
        return this;
    }
    public MaskLayer enable(Constants.EnableMask mask){return enable(mask, true);}
    public MaskLayer disable(Constants.EnableMask mask){return enable(mask, false);}
    public MaskLayer enableBlend(boolean value){return enable(Constants.EnableMask.BLEND, value);}
    public MaskLayer enableBlend(){return enableBlend(true);}
    public MaskLayer disableBlend(){return enableBlend(false);}
    public MaskLayer enableCullFace(boolean value){return enable(Constants.EnableMask.CULL_FACE, value);}
    public MaskLayer enableCullFace(){return enableCullFace(true);}
    public MaskLayer disableCullFace(){return enableCullFace(false);}
    public MaskLayer enableDepthTest(boolean value){return enable(Constants.EnableMask.DEPTH_TEST, value);}
    public MaskLayer enableDepthTest(){return enableDepthTest(true);}
    public MaskLayer disableDepthTest(){return enableDepthTest(false);}
    @Override public void close() {
        for(ObjectBooleanImmutablePair<Constants.EnableMask> pair : masks){
            pair.left().enable(pair.rightBoolean());
        }
    }
}
