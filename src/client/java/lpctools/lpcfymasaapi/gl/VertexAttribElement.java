package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

public record VertexAttribElement(int count, Constants.DataType type, boolean normalized) {
    int getSize() {return count * type.size;}
    void vertexAttrib(int index, int stride, int offset) {
        GL45.glVertexAttribPointer(index, count, type.value, normalized, stride, offset);
    }
    void vertexAttribAndEnable(int index, int stride, int offset){
        vertexAttrib(index, stride, offset);
        GL45.glEnableVertexAttribArray(index);
    }
}
