package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;

public class Program<T extends VertexAttrib> implements AutoCloseable{
    public final Shader vertexShader;
    public final Shader fragmentShader;
    public final T attrib;
    protected final ArrayList<Uniform> uniforms = new ArrayList<>();
    private int glProgramId;
    public int getGlProgramId(){return glProgramId;}
    public Program(Shader vertexShader, Shader fragmentShader, T attrib){
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.attrib = attrib;
        LPCGLInitializer.programs.add(this);
        if(initialized()) attachAndLink();
    }
    @Override public void close() {
        LPCGLInitializer.programs.remove(this);
        GL45.glDeleteProgram(glProgramId);
        glProgramId = 0;
    }
    public void useAndUniform(){
        GL45.glUseProgram(glProgramId);
        for(Uniform uniform : uniforms)
            uniform.uniform();
    }
    protected <U extends Uniform> U addUniform(U uniform){
        uniforms.add(uniform);
        return uniform;
    }
    void attachAndLink() {
        if(glProgramId == 0) glProgramId = GL45.glCreateProgram();
        GL45.glAttachShader(glProgramId, vertexShader.getGlShaderId());
        GL45.glAttachShader(glProgramId, fragmentShader.getGlShaderId());
        GL45.glLinkProgram(glProgramId);
    }
}
