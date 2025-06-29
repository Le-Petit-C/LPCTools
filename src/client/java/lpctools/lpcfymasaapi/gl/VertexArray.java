package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;

public class VertexArray implements AutoCloseable{
    private int glVertexArrayId = 0;
    @SuppressWarnings("unused")
    public int getGlVertexArrayId() {return glVertexArrayId;}
    public VertexArray(){
        LPCGLInitializer.vertexArrays.add(this);
        if(initialized()) gen();
    }
    @Override public void close() {
        LPCGLInitializer.vertexArrays.remove(this);
        GL45.glDeleteVertexArrays(glVertexArrayId);
        glVertexArrayId = 0;
    }
    void bind(){GL45.glBindVertexArray(glVertexArrayId);}
    void gen(){if(glVertexArrayId == 0) glVertexArrayId = GL45.glGenVertexArrays();}
}
