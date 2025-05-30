package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import lpctools.render.LPCExtraPipelines;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 16;}
    @Override public int getVertexCountPerBlock(){return 4;}
    @Override public int getIndexBufferSizePerBlockByInt(){return 6;}
    @Override public String getNameKey() {
        return "fullSurface";
    }
    @Override public RenderPipeline getShader(boolean xray) {
        if(xray) return MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH_NO_CULL;
        else return LPCExtraPipelines.POSITION_COLOR_MASA_NO_CULL;
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
