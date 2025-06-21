package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;
import static lpctools.lpcfymasaapi.gl.Constants.*;
import static lpctools.lpcfymasaapi.gl.Constants.BufferType.*;

@SuppressWarnings("unused")
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
    public void bindAsArray(){ARRAY_BUFFER.bind(this);}
    public void unbindAsArray(){ARRAY_BUFFER.unbind();}
    public static void unbindAsArrayStatic(){ARRAY_BUFFER.unbind();}
    public void bindAsElementArray(){ELEMENT_ARRAY_BUFFER.bind(this);}
    public void unbindAsElementArray(){ELEMENT_ARRAY_BUFFER.unbind();}
    public static void unbindAsElementArrayStatic(){ELEMENT_ARRAY_BUFFER.unbind();}
    public void bindAsTexture(){TEXTURE_BUFFER.bind(this);}
    public void unbindAsTexture(){TEXTURE_BUFFER.unbind();}
    public static void unbindAsTextureStatic(){TEXTURE_BUFFER.unbind();}
    void gen(){if(glBufferId == 0) glBufferId = GL45.glCreateBuffers();}
}
