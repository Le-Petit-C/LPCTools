package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;

public class Buffer implements AutoCloseable{
    private int glBufferId = 0;
    @SuppressWarnings("unused")
    public int getGlBufferId(){return glBufferId;}
    public Buffer(){
        LPCGLInitializer.buffers.add(this);
        if(initialized()) gen();
    }
    @Override public void close(){
        LPCGLInitializer.buffers.remove(this);
        GL45.glDeleteBuffers(glBufferId);
        glBufferId = 0;
    }
    //usage:STATIC_DRAW,STATIC_READ,etc.
    public void data(ByteBuffer data, Constants.BufferMode usage){
        GL45.glNamedBufferData(glBufferId, data, usage.value);}
    public void bind(int target){GL45.glBindBuffer(target, glBufferId);}
    public void unbind(int target){GL45.glBindBuffer(target, 0);}
    public void bindAsArray(){bind(GL45.GL_ARRAY_BUFFER);}
    @SuppressWarnings("unused")
    public void unbindAsArray(){unbind(GL45.GL_ARRAY_BUFFER);}
    public void bindAsElementArray(){bind(GL45.GL_ELEMENT_ARRAY_BUFFER);}
    @SuppressWarnings("unused")
    public void unbindAsElementArray(){unbind(GL45.GL_ELEMENT_ARRAY_BUFFER);}
    void gen(){if(glBufferId == 0) glBufferId = GL45.glCreateBuffers();}
}
