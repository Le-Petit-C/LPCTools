package lpctools.tools.canSpawnDisplay;

import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public interface IRenderMethod {
    int getVertexBufferSizePerVertex();
    int getVertexCountPerBlock();
    int getIndexCountPerBlock();
    String getNameKey();
    ShaderProgramKey getShader();
    void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray);
}
