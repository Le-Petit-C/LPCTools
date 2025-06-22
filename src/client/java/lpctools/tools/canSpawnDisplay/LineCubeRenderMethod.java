package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;

import java.nio.ByteBuffer;

public class LineCubeRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSize() {return 12 * 8;}
    @Override public int getIndexCount() {return 24;}
    @Override public String getNameKey() {return "lineCube";}
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.LINES;}
    @Override public void vertex(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        vertexBuffer.putFloat(-0.4f).putFloat(-0.4f).putFloat(-0.4f);
        vertexBuffer.putFloat(-0.4f).putFloat(-0.4f).putFloat(0.4f);
        vertexBuffer.putFloat(-0.4f).putFloat(0.4f).putFloat(-0.4f);
        vertexBuffer.putFloat(-0.4f).putFloat(0.4f).putFloat(0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(-0.4f).putFloat(-0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(-0.4f).putFloat(0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(0.4f).putFloat(-0.4f);
        vertexBuffer.putFloat(0.4f).putFloat(0.4f).putFloat(0.4f);
        indexBuffer.put((byte)0).put((byte)1);
        indexBuffer.put((byte)2).put((byte)3);
        indexBuffer.put((byte)4).put((byte)5);
        indexBuffer.put((byte)6).put((byte)7);
        indexBuffer.put((byte)0).put((byte)2);
        indexBuffer.put((byte)1).put((byte)3);
        indexBuffer.put((byte)4).put((byte)6);
        indexBuffer.put((byte)5).put((byte)7);
        indexBuffer.put((byte)0).put((byte)4);
        indexBuffer.put((byte)1).put((byte)5);
        indexBuffer.put((byte)2).put((byte)6);
        indexBuffer.put((byte)3).put((byte)7);
    }
}
