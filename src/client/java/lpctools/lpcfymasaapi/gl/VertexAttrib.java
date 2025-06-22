package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("unused")
public class VertexAttrib implements IterableElement{
    private final VertexAttribElement[] elements;
    private final int[] defaultOffsets;
    private final int defaultStride;
    public VertexAttrib(IterableElement... elements){
        ArrayList<VertexAttribElement> elementArrayList = new ArrayList<>();
        IntArrayList defaultOffsetsArrayList = new IntArrayList();
        MutableInt vertexSize = new MutableInt(0);
        for(Iterable<VertexAttribElement> attrib : elements)
            attrib.forEach(element->{
                elementArrayList.add(element);
                defaultOffsetsArrayList.add(vertexSize.intValue());
                vertexSize.add(element.getSize());
            });
        this.defaultOffsets = defaultOffsetsArrayList.toArray(new int[0]);
        this.elements = elementArrayList.toArray(new VertexAttribElement[0]);
        this.defaultStride = vertexSize.intValue();
    }
    //有可能前面几个attrib被占据了，从给定索引开始attribAndEnable，具体功能看实现
    public void attribAndEnableShifted(int start){
        int offset = 0;
        for(int index = 0; index < elements.length; ++index){
            VertexAttribElement element = elements[index];
            element.vertexAttribAndEnable(index + start, defaultStride, offset);
            offset += element.getSize();
        }
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
    @Contract(pure = true)
    public int getSize(){return defaultStride;}
    @Contract(pure = true)
    public int getCount(){return elements.length;}
    
    @Override public @NotNull Iterator<VertexAttribElement> iterator() {
        return Arrays.stream(elements).iterator();
    }
}
