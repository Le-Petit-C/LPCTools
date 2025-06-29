package lpctools.lpcfymasaapi.gl.furtherWarpped;

import lpctools.lpcfymasaapi.gl.*;

import java.nio.ByteBuffer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RenderBuffer<T extends Program> implements AutoCloseable{
    public final Constants.BufferMode bufferMode;
    public final T program;
    public RenderBuffer(Constants.BufferMode bufferMode, T program){
        this.bufferMode = bufferMode;
        this.program = program;
    }
    public void renderWithIndexes(Constants.DrawMode drawMode, int count, MaskLayer layer){
        initialize(layer);
        layer.bindArray(vertexArray);
        program.useAndUniform();
        drawMode.drawElements(count, Constants.IndexType.INT);
    }
    public void render(Constants.DrawMode drawMode, int count, MaskLayer layer){
        initialize(layer);
        layer.bindArray(vertexArray);
        program.useAndUniform();
        drawMode.drawArrays(0, count);
    }
    @Override public void close() {
        vertexArray.close();
        vertexBuffer.close();
        indexBuffer.close();
    }
    public void dataIndex(ByteBuffer buffer){
        indexBuffer.data(buffer, bufferMode);
    }
    public void dataVertex(ByteBuffer buffer){
        vertexBuffer.data(buffer, bufferMode);
    }
    protected void initialize(MaskLayer layer) {
        if (initialized) return;
        layer.bindArray(vertexArray);
        indexBuffer.bindAsElementArray();
        vertexBuffer.bindAsArray();
        program.attrib.attribAndEnable();
        initialized = true;
    }
    private boolean initialized = false;
    private final Buffer indexBuffer = new Buffer();
    private final Buffer vertexBuffer = new Buffer();
    private final VertexArray vertexArray = new VertexArray();
}
