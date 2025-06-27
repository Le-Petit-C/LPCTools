package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;

import java.nio.ByteBuffer;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSize() {return 12 * 4;}
    @Override public int getIndexCount() {return 4;}
    @Override public String getNameKey() {return "fullSurface";}
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.TRIANGLE_FAN;}
    @Override public void vertex(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        vertexBuffer.putFloat(-0.5f).putFloat(-0.495f).putFloat(-0.5f);
        vertexBuffer.putFloat(-0.5f).putFloat(-0.495f).putFloat(0.5f);
        vertexBuffer.putFloat(0.5f).putFloat(-0.495f).putFloat(0.5f);
        vertexBuffer.putFloat(0.5f).putFloat(-0.495f).putFloat(-0.5f);
        indexBuffer.put((byte) 0).put((byte) 1).put((byte) 2).put((byte) 3);
    }
}
