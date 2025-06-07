package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.Constants;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public interface IRenderMethod {
    int getVertexBufferSizePerVertex();
    int getVertexCountPerBlock();
    int getIndexCountPerBlock();
    String getNameKey();
    Constants.DrawMode getDrawMode();
    void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray);
}
