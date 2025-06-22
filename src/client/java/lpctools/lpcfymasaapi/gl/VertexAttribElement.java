package lpctools.lpcfymasaapi.gl;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL45;

import java.util.Iterator;

public record VertexAttribElement(int count, Constants.DataType type, boolean normalized) implements IterableElement {
    public int getSize() {return count * type.size;}
    public void vertexAttrib(int index, int stride, int offset) {
        GL45.glVertexAttribPointer(index, count, type.value, normalized, stride, offset);
    }
    public void vertexAttribAndEnable(int index, int stride, int offset){
        vertexAttrib(index, stride, offset);
        GL45.glEnableVertexAttribArray(index);
    }
    @Override public @NotNull Iterator<VertexAttribElement> iterator() {
        return new Iterator<>() {
            boolean hasNext = true;
            @Override public boolean hasNext() {
                return hasNext;
            }
            @Override public VertexAttribElement next() {
                hasNext = false;
                return VertexAttribElement.this;
            }
        };
    }
}
