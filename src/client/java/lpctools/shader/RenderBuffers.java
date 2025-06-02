package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.furtherWarpped.RenderBuffer;
import org.joml.Matrix4f;

public class RenderBuffers {
    public static SimpleRenderBuffer simpleRenderBuffer(Constants.BufferMode bufferMode){
        return new SimpleRenderBuffer(bufferMode);
    }
    
    public static class SimpleRenderBuffer extends RenderBuffer<ShaderPrograms.SimpleProgram> implements ShaderPrograms.WithProjectionMatrix, ShaderPrograms.WithModelViewMatrix{
        public SimpleRenderBuffer(Constants.BufferMode bufferMode) {super(bufferMode, ShaderPrograms.SIMPLE_PROGRAM);}
        @Override public void setModelMatrix(Matrix4f matrix) {program.setModelMatrix(matrix);}
        @Override public void setProjectionMatrix(Matrix4f matrix) {program.setProjectionMatrix(matrix);}
    }
}
