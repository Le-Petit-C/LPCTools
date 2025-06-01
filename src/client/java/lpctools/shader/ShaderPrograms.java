package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Program;
import lpctools.lpcfymasaapi.gl.furtherWarpped.VertexTypes;
import org.joml.Matrix4f;

import static lpctools.lpcfymasaapi.gl.Uniform.*;

public class ShaderPrograms {
    public static final SimpleProgram SIMPLE_PROGRAM = new SimpleProgram();
    
    public interface WithProjectionMatrix{ void setProjectionMatrix(Matrix4f matrix);}
    public interface WithModelViewMatrix{ void setModelMatrix(Matrix4f matrix);}
    
    public static class SimpleProgram extends Program<VertexTypes.PositionColor> implements WithProjectionMatrix, WithModelViewMatrix{
        public final UniformMatrix4f projUniform = addUniform(new UniformMatrix4f(this, "projectionMatrix"));
        public final UniformMatrix4f modUniform = addUniform(new UniformMatrix4f(this, "modelViewMatrix"));
        public SimpleProgram() {super(VertexShaders.simple_translation, FragmentShaders.no_change, VertexTypes.POSITION_COLOR);}
        @Override public void setProjectionMatrix(Matrix4f matrix) {projUniform.set(matrix);}
        @Override public void setModelMatrix(Matrix4f matrix) {modUniform.set(matrix);}
    }
    
    public static void init(){}
}
