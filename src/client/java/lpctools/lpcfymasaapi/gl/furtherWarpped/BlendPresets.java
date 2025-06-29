package lpctools.lpcfymasaapi.gl.furtherWarpped;

import lpctools.lpcfymasaapi.gl.Constants;

@SuppressWarnings("unused")
public interface BlendPresets {
    //常用混合预设
    BlendPreset STANDARD_ALPHA = new SimpleBlendPreset(
        Constants.BlendFactor.SRC_ALPHA, Constants.BlendFactor.ONE_MINUS_SRC_ALPHA,
        Constants.BlendFactor.SRC_ALPHA, Constants.BlendFactor.ONE_MINUS_SRC_ALPHA,
        Constants.BlendEquation.ADD,     Constants.BlendEquation.ADD
    );
    BlendPreset ADDITIVE = new SimpleBlendPreset(
        Constants.BlendFactor.ONE,       Constants.BlendFactor.ONE,
        Constants.BlendFactor.ONE,       Constants.BlendFactor.ONE,
        Constants.BlendEquation.ADD,     Constants.BlendEquation.ADD
    );
    BlendPreset PREMULTIPLIED_ALPHA = new SimpleBlendPreset(
        Constants.BlendFactor.ONE,       Constants.BlendFactor.ONE_MINUS_SRC_ALPHA,
        Constants.BlendFactor.ONE,       Constants.BlendFactor.ONE_MINUS_SRC_ALPHA,
        Constants.BlendEquation.ADD,     Constants.BlendEquation.ADD
    );
    
    //混合预设接口
    interface BlendPreset {
        //应用此混合预设
        void apply();
    }
    
    //简单混合预设实现
    class SimpleBlendPreset implements BlendPreset {
        private final Constants.BlendFactor srcRGB, dstRGB, srcA, dstA;
        private final Constants.BlendEquation eqRGB, eqA;
        
        public SimpleBlendPreset(Constants.BlendFactor srcRGB, Constants.BlendFactor dstRGB,
                                 Constants.BlendFactor srcA, Constants.BlendFactor dstA,
                                 Constants.BlendEquation eqRGB, Constants.BlendEquation eqA) {
            this.srcRGB = srcRGB;
            this.dstRGB = dstRGB;
            this.srcA   = srcA;
            this.dstA   = dstA;
            this.eqRGB  = eqRGB;
            this.eqA    = eqA;
        }
        
        @Override
        public void apply() {
            GlStatics.setBlend(srcRGB, dstRGB, srcA, dstA, eqRGB, eqA);
        }
    }
}
