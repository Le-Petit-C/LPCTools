package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.furtherWarpped.ArrayListCachedRenderBuffer;
import org.joml.Matrix4f;

@SuppressWarnings("unused")
public class RenderBuffers {
    public static PositionColorBuffer positionColorBuffer(Constants.BufferMode bufferMode){
        return new PositionColorBuffer(bufferMode);
    }
    public static PositionStaticColorBuffer positionStaticColorBuffer(Constants.BufferMode bufferMode){
        return new PositionStaticColorBuffer(bufferMode);
    }
    public static class PositionColorBuffer extends ArrayListCachedRenderBuffer<ShaderPrograms.PositionColorProgram> implements ShaderPrograms.WithFinalMatrix{
        public PositionColorBuffer(Constants.BufferMode bufferMode) {super(bufferMode, ShaderPrograms.POSITION_COLOR_PROGRAM);}
        @Override public void setFinalMatrix(Matrix4f matrix) {program.setFinalMatrix(matrix);}
    }
    public static class PositionStaticColorBuffer extends ArrayListCachedRenderBuffer<ShaderPrograms.PositionStaticColorProgram> implements ShaderPrograms.WithFinalMatrix{
        public PositionStaticColorBuffer(Constants.BufferMode bufferMode) {super(bufferMode, ShaderPrograms.POSITION_STATIC_COLOR_PROGRAM);}
        @Override public void setFinalMatrix(Matrix4f matrix) {program.setFinalMatrix(matrix);}
    }
}
