package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@SuppressWarnings("unused")
public class VertexAttrib {
    private final VertexAttribElement[] elements;
    private final int[] defaultOffsets;
    private final int defaultStride;
    public VertexAttrib(VertexAttribElement... elements){
        this.elements = elements.clone();
        defaultOffsets = new int[elements.length];
        int vertexSize = 0;
        for(int a = 0; a < elements.length; ++a){
            defaultOffsets[a] = vertexSize;
            vertexSize += elements[a].getSize();
        }
        this.defaultStride = vertexSize;
    }
    public void attribAndEnable(){
        int offset = 0;
        for(int index = 0; index < elements.length; ++index){
            VertexAttribElement element = elements[index];
            element.vertexAttribAndEnable(index, defaultStride, offset);
            offset += element.getSize();
        }
    }
    public void attribAndEnable(int index){
        attribAndEnable(index, defaultStride, defaultOffsets[index]);
    }
    public void attribAndEnable(int index, int stride, int offset){
        elements[index].vertexAttribAndEnable(index, stride, offset);
    }
    public void attribAndEnable(Buffer buffer, int index){
        buffer.bindAsArray();
        attribAndEnable(index);
    }
    public void attribAndEnable(Buffer buffer, int index, int stride, int offset){
        buffer.bindAsArray();
        attribAndEnable(index, stride, offset);
    }
    public void attribAndEnable(Buffer... buffers){
        if(buffers.length != elements.length) throw new RuntimeException("attribAndEnable: Buffers length doesn't matches elements length");
        Object2IntOpenHashMap<Buffer> strides = new Object2IntOpenHashMap<>();
        for(int a = 0; a < elements.length; ++a){
            Buffer buffer = buffers[a];
            VertexAttribElement e = elements[a];
            strides.put(buffer, e.getSize() + strides.getOrDefault(buffer, 0));
        }
        Object2IntOpenHashMap<Buffer> offsets = new Object2IntOpenHashMap<>();
        for(int a = 0; a < elements.length; ++a){
            Buffer buffer = buffers[a];
            VertexAttribElement e = elements[a];
            buffer.bindAsArray();
            e.vertexAttribAndEnable(a, strides.getInt(buffer), offsets.getInt(buffer));
            offsets.put(buffer, e.getSize() + offsets.getOrDefault(buffer, 0));
        }
    }
    public int getSize(){return defaultStride;}
}
