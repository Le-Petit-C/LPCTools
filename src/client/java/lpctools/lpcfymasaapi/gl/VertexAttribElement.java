package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

public record VertexAttribElement(int count, Constants.DataType type, boolean normalized) {
    int getSize() {return count * type.size;}
    void vertexAttrib(int index, int vertexSize, int offset) {
        GL45.glVertexAttribPointer(index, count, type.value, normalized, vertexSize, offset);
    }
}
