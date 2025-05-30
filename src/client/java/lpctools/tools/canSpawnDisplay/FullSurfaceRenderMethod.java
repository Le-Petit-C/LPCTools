package lpctools.tools.canSpawnDisplay;

import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 16;}
    @Override public int getVertexCountPerBlock(){return 4;}
    @Override public int getIndexCountPerBlock(){return 6;}
    @Override public String getNameKey() {
        return "fullSurface";
    }
    @Override public ShaderProgramKey getShader() {
        return ShaderProgramKeys.POSITION_COLOR;
    }
    @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray) {
        double yOffset = xray ? 0 : 0.005;
        float y = (float) (pos.getY() + yOffset);
        float minX = pos.getX(), maxX = pos.getX() + 1;
        float minZ = pos.getZ(), maxZ = pos.getZ() + 1;
        indexBuffer.putInt(index).putInt(index + 1).putInt(index + 2);
        indexBuffer.putInt(index).putInt(index + 2).putInt(index + 3);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ).putInt(0xffffffff);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ).putInt(0xffffffff);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ).putInt(0xffffffff);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ).putInt(0xffffffff);
    }
}
