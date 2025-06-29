package lpctools.lpcfymasaapi.gl.furtherWarpped;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.MaskLayer;
import lpctools.lpcfymasaapi.gl.Program;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ArrayListCachedRenderBuffer<T extends Program> extends RenderBuffer<T>{
    public ArrayListCachedRenderBuffer(Constants.BufferMode bufferMode, T program) {
        super(bufferMode, program);
    }
    public void clearVertex(){vertexArrayBuffer.clear();}
    public void clearIndex(){indexArrayBuffer.clear();}
    public void clear(){
        indexArrayBuffer.clear();
        vertexArrayBuffer.clear();
    }
    public void renderWithIndexes(Constants.DrawMode drawMode, MaskLayer layer){
        if(dirty) refresh(layer);
        super.renderWithIndexes(drawMode, indexArrayBuffer.size(), layer);
    }
    public void render(Constants.DrawMode drawMode, MaskLayer layer){
        if(dirty) refresh(layer);
        super.render(drawMode, vertexArrayBuffer.size() / program.attrib.getSize(), layer);
    }
    public void refresh(MaskLayer layer){
        initialize(layer);
        int size = Math.max(indexArrayBuffer.size() * 4, vertexArrayBuffer.size());
        ByteBuffer buffer = MemoryUtil.memAlloc(size);
        for(int index : indexArrayBuffer) buffer.putInt(index);
        buffer.flip();
        dataIndex(buffer);
        buffer.clear();
        for(byte b : vertexArrayBuffer) buffer.put(b);
        buffer.flip();
        dataVertex(buffer);
        MemoryUtil.memFree(buffer);
        dirty = false;
    }
    public ArrayListCachedRenderBuffer<T> putIndex(int index){indexArrayBuffer.add(index);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putByte(byte b){_putByte(b);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putBytes(byte... bytes){for(byte b : bytes) _putByte(b);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putInt(int n){_putInt(n);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putInts(int... ints){for(int i : ints) _putInt(i);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putFloat(float f){_putFloat(f);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putFloats(float... floats){for(float f : floats) _putFloat(f);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putLong(long n){_putLong(n);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putLongs(long... longs){for(long i : longs) _putLong(i);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putDouble(double d){_putDouble(d);dirty = true;return this;}
    public ArrayListCachedRenderBuffer<T> putDoubles(double... doubles){for(double d : doubles) _putDouble(d);dirty = true;return this;}
    //下面这几个方法不会markDirty，用的时候要注意自行mark一下
    public void _putByte(byte b){vertexArrayBuffer.add(b);}
    public void _putInt(int n){
        _putByte((byte)n);
        _putByte((byte)(n >>> 8));
        _putByte((byte)(n >>> 16));
        _putByte((byte)(n >>> 24));
    }
    public void _putFloat(float f){_putInt(Float.floatToRawIntBits(f));}
    public void _putLong(long n){
        _putByte((byte)n);
        _putByte((byte)(n >>> 8));
        _putByte((byte)(n >>> 16));
        _putByte((byte)(n >>> 24));
        _putByte((byte)(n >>> 32));
        _putByte((byte)(n >>> 40));
        _putByte((byte)(n >>> 48));
        _putByte((byte)(n >>> 56));
    }
    public void _putDouble(double f){_putLong(Double.doubleToRawLongBits(f));}
    public void markDirty(){dirty = true;}
    private boolean dirty;
    private final IntArrayList indexArrayBuffer = new IntArrayList();
    private final ByteArrayList vertexArrayBuffer = new ByteArrayList();
}
