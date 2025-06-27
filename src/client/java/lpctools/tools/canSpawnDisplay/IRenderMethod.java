package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;

import java.nio.ByteBuffer;

//索引使用GL_UNSIGNED_BYTE
public interface IRenderMethod {
    int getVertexBufferSize();
    int getIndexCount();
    String getNameKey();
    Constants.DrawMode getDrawMode();
    //vertex但是不flip
    void vertex(ByteBuffer vertexBuffer, ByteBuffer indexBuffer);
}
