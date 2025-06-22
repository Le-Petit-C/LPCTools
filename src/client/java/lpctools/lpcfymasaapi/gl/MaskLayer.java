package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;

import java.util.ArrayList;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MaskLayer implements AutoCloseable{
    private final BooleanArrayList booleanArrayList = new BooleanArrayList();
    private final ArrayList<Constants.EnableOption> masks;
    public MaskLayer(){masks = new ArrayList<>();}
    public MaskLayer enable(Constants.EnableOption option, boolean value){
        masks.add(option);
        option.push(booleanArrayList);
        option.enable(value);
        return this;
    }
    public MaskLayer enable(Constants.EnableOption mask){return enable(mask, true);}
    public MaskLayer disable(Constants.EnableOption mask){return enable(mask, false);}
    public MaskLayer enableBlend(boolean value){return enable(Constants.EnableMask.BLEND, value);}
    public MaskLayer enableBlend(){return enableBlend(true);}
    public MaskLayer disableBlend(){return enableBlend(false);}
    public MaskLayer enableCullFace(boolean value){return enable(Constants.EnableMask.CULL_FACE, value);}
    public MaskLayer enableCullFace(){return enableCullFace(true);}
    public MaskLayer disableCullFace(){return enableCullFace(false);}
    public MaskLayer enableDepthTest(boolean value){return enable(Constants.EnableMask.DEPTH_TEST, value);}
    public MaskLayer enableDepthTest(){return enableDepthTest(true);}
    public MaskLayer disableDepthTest(){return enableDepthTest(false);}
    public MaskLayer enableDepthWrite(boolean value){return enable(Constants.EnableOptions.DEPTH_WRITE, value);}
    public MaskLayer enableDepthWrite(){return enableDepthWrite(true);}
    public MaskLayer disableDepthWrite(){return enableDepthWrite(false);}
    public MaskLayer enableColorWrite(boolean value){return enable(Constants.EnableOptions.COLOR_WRITE, value);}
    public MaskLayer enableColorWrite(){return enableColorWrite(true);}
    public MaskLayer disableColorWrite(){return enableColorWrite(false);}
    @Override public void close() {
        for(Constants.EnableOption option : masks)
            option.pop(booleanArrayList);
    }
}
