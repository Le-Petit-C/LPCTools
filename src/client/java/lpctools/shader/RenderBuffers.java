package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.furtherWarpped.RenderBuffer;
import lpctools.lpcfymasaapi.gl.furtherWarpped.VertexTypes;

public class RenderBuffers {
    public static SimpleRenderBuffer simpleRenderBuffer(Constants.BufferMode bufferMode){
        return new SimpleRenderBuffer(bufferMode);
    }
    
    public static class SimpleRenderBuffer extends RenderBuffer<VertexTypes.PositionColor, ShaderPrograms.SimpleProgram>{
        public SimpleRenderBuffer(Constants.BufferMode bufferMode) {super(bufferMode, ShaderPrograms.SIMPLE_PROGRAM);}
    }
}
