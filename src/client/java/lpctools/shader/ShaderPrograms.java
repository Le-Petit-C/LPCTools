package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Program;

import static lpctools.lpcfymasaapi.gl.Uniforms.*;

public class ShaderPrograms {
    public interface WithProjectionMatrix{ UniformMatrix4f getProjMatUniform();}
    public interface WithModelViewMatrix{ UniformMatrix4f getModMatUniform();}
    public static void init(){}
    public static final SimpleProgram simple_program = new SimpleProgram();
    public static class SimpleProgram extends Program implements WithProjectionMatrix, WithModelViewMatrix{
        public final UniformMatrix4f projUniform = addUniform(new UniformMatrix4f(this, "projectionMatrix"));
        public final UniformMatrix4f modUniform = addUniform(new UniformMatrix4f(this, "modelViewMatrix"));
        public SimpleProgram() {super(VertexShaders.simple_translation, FragmentShaders.no_change);}
        @Override public UniformMatrix4f getProjMatUniform() {return projUniform;}
        @Override public UniformMatrix4f getModMatUniform() {return modUniform;}
    }
}
