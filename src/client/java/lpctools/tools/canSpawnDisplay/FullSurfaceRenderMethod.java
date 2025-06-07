package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 12;}
    @Override public int getVertexCountPerBlock(){return 4;}
    @Override public int getIndexCountPerBlock(){return 6;}
    @Override public String getNameKey() {
        return "fullSurface";
    }
    @Override public Constants.DrawMode getDrawMode(){return Constants.DrawMode.TRIANGLES;}
    @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray) {
        double yOffset = xray ? 0 : 0.005;
        float y = (float) (pos.getY() + yOffset);
        float minX = pos.getX(), maxX = pos.getX() + 1;
        float minZ = pos.getZ(), maxZ = pos.getZ() + 1;
        indexBuffer.putInt(index).putInt(index + 1).putInt(index + 2);
        indexBuffer.putInt(index).putInt(index + 2).putInt(index + 3);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ);
    }
}
