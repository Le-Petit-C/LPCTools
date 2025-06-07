package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 12;}
    @Override public int getVertexCountPerBlock(){return 4;}
    @Override public int getIndexCountPerBlock(){return 8;}
    @Override public String getNameKey() {
        return "minihudStyle";
    }
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.LINES;}
    @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray) {
        double yOffset = xray ? 0 : 0.005;
        float y = (float) (pos.getY() + yOffset);
        float minX = (float) (pos.getX() + 0.1), maxX = (float) (pos.getX() + 0.9);
        float minZ = (float) (pos.getZ() + 0.1), maxZ = (float) (pos.getZ() + 0.9);
        indexBuffer.putInt(index).putInt(index + 1);
        indexBuffer.putInt(index + 1).putInt(index + 2);
        indexBuffer.putInt(index + 2).putInt(index + 3);
        indexBuffer.putInt(index + 3).putInt(index);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ);
    }
}
