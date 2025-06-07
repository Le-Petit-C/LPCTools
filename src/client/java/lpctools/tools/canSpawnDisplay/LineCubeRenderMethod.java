package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class LineCubeRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 12;}
    @Override public int getVertexCountPerBlock(){return 8;}
    @Override public int getIndexCountPerBlock(){return 24;}
    @Override public String getNameKey() {
        return "lineCube";
    }
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.LINES;}
    @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray) {
        float minX = (float) (pos.getX() + 0.1), maxX = (float) (pos.getX() + 0.9);
        float minY = (float) (pos.getY() + 0.1), maxY = (float) (pos.getY() + 0.9);
        float minZ = (float) (pos.getZ() + 0.1), maxZ = (float) (pos.getZ() + 0.9);
        indexBuffer.putInt(index).putInt(index + 1);
        indexBuffer.putInt(index + 2).putInt(index + 3);
        indexBuffer.putInt(index + 4).putInt(index + 5);
        indexBuffer.putInt(index + 6).putInt(index + 7);
        indexBuffer.putInt(index).putInt(index + 2);
        indexBuffer.putInt(index + 1).putInt(index + 3);
        indexBuffer.putInt(index + 4).putInt(index + 6);
        indexBuffer.putInt(index + 5).putInt(index + 7);
        indexBuffer.putInt(index).putInt(index + 4);
        indexBuffer.putInt(index + 1).putInt(index + 5);
        indexBuffer.putInt(index + 2).putInt(index + 6);
        indexBuffer.putInt(index + 3).putInt(index + 7);
        vertexBuffer.putFloat(minX).putFloat(minY).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(minZ);
        vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(minZ);
        vertexBuffer.putFloat(minX).putFloat(minY).putFloat(maxZ);
        vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(maxZ);
        vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(maxZ);
        vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(maxZ);
    }
}
