package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;

import java.nio.ByteBuffer;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSize() {return 12 * 4;}
    @Override public int getIndexCount() {return 4;}
    @Override public String getNameKey() {return "minihudStyle";}
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.LINE_LOOP;}
    @Override public void vertex(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        vertexBuffer.putFloat(-0.4f).putFloat(-0.495f).putFloat(-0.4f);
        vertexBuffer.putFloat(-0.4f).putFloat(-0.495f).putFloat(0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(-0.495f).putFloat(0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(-0.495f).putFloat(-0.4f);
        indexBuffer.put((byte) 0).put((byte) 1).put((byte) 2).put((byte) 3);
    }
}
