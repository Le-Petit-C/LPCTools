package lpctools.lpcfymasaapi.gl;

import org.lwjgl.opengl.GL45;

public class VertexAttrib {
    private final VertexAttribElement[] elements;
    private final int vertexSize;
    public VertexAttrib(VertexAttribElement... elements){
        this.elements = elements.clone();
        int vertexSize = 0;
        for(VertexAttribElement element : elements)
            vertexSize += element.getSize();
        this.vertexSize = vertexSize;
    }
    public void attribAndEnable(){
        int offset = 0;
        for(int index = 0; index < elements.length; ++index){
            VertexAttribElement element = elements[index];
            element.vertexAttrib(index, vertexSize, offset);
            offset += element.getSize();
            GL45.glEnableVertexAttribArray(index);
        }
    }
    public int getSize(){return vertexSize;}
}
