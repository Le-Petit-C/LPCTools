package lpctools.lpcfymasaapi.gl.furtherWarpped;

import com.mojang.blaze3d.platform.GlConst;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.lpcfymasaapi.gl.*;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class RenderBuffer<T extends VertexAttrib, U extends Program<T>> implements AutoCloseable{
    public final Constants.BufferMode bufferMode;
    public final U program;
    public RenderBuffer(Constants.BufferMode bufferMode, U program){
        this.bufferMode = bufferMode;
        this.program = program;
        vertexArray.bind();
        indexBuffer.bindAsElementArray();
        vertexBuffer.bindAsArray();
        program.attrib.attribAndEnable();
        vertexArray.unbind();
    }
    public void renderWithIndexes(Constants.DrawMode drawMode){
        if(dirty) refresh();
        vertexArray.bind();
        program.useAndUniform();
        GL45.glDrawElements(drawMode.value, indexArrayBuffer.size(), GlConst.GL_UNSIGNED_INT, 0);
        vertexArray.unbind();
    }
    public void render(Constants.DrawMode drawMode){
        if(dirty) refresh();
        vertexArray.bind();
        program.useAndUniform();
        GL45.glDrawArrays(drawMode.value, 0, vertexArrayBuffer.size() / program.attrib.getSize());
        vertexArray.unbind();
    }
    @Override public void close() throws Exception {
        vertexArray.close();
        vertexBuffer.close();
        indexBuffer.close();
    }
    public void clearVertex(){vertexArrayBuffer.clear();}
    public void clearIndex(){indexArrayBuffer.clear();}
    public void clear(){
        indexArrayBuffer.clear();
        vertexArrayBuffer.clear();
    }
    void refresh(){
        int size = Math.max(indexArrayBuffer.size() * 4, vertexArrayBuffer.size());
        ByteBuffer buffer = MemoryUtil.memAlloc(size);
        for(int index : indexArrayBuffer) buffer.putInt(index);
        buffer.flip();
        indexBuffer.data(buffer, bufferMode);
        buffer.clear();
        for(byte b : vertexArrayBuffer) buffer.put(b);
        buffer.flip();
        vertexBuffer.data(buffer, bufferMode);
        MemoryUtil.memFree(buffer);
        dirty = false;
    }
    public RenderBuffer<T, U> putIndex(int index){indexArrayBuffer.add(index);dirty = true;return this;}
    public RenderBuffer<T, U> putByte(byte b){_putByte(b);dirty = true;return this;}
    public RenderBuffer<T, U> putBytes(byte... bytes){for(byte b : bytes) _putByte(b);dirty = true;return this;}
    public RenderBuffer<T, U> putInt(int n){_putInt(n);dirty = true;return this;}
    public RenderBuffer<T, U> putInts(int... ints){for(int i : ints) _putInt(i);dirty = true;return this;}
    public RenderBuffer<T, U> putFloat(float f){_putFloat(f);dirty = true;return this;}
    public RenderBuffer<T, U> putFloats(float... floats){for(float f : floats) _putFloat(f);dirty = true;return this;}
    //下面这几个方法不会markDirty，用的时候要注意自行mark一下
    public void _putByte(byte b){vertexArrayBuffer.add(b);}
    public void _putInt(int n){
        _putByte((byte)n);
        _putByte((byte)(n >>> 8));
        _putByte((byte)(n >>> 16));
        _putByte((byte)(n >>> 24));
    }
    public void _putFloat(float f){_putInt(Float.floatToRawIntBits(f));}
    public void markDirty(){dirty = true;}
    private boolean dirty;
    private final IntArrayList indexArrayBuffer = new IntArrayList();
    private final ByteArrayList vertexArrayBuffer = new ByteArrayList();
    private final Buffer indexBuffer = new Buffer();
    private final Buffer vertexBuffer = new Buffer();
    private final VertexArray vertexArray = new VertexArray();
}
