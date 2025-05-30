package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public interface IRenderMethod {
    int getVertexBufferSizePerVertex();
    int getVertexCountPerBlock();
    int getIndexBufferSizePerBlockByInt();
    String getNameKey();
    RenderPipeline getShader(boolean xray);
    void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray);
}
