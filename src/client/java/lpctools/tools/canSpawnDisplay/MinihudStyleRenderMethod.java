package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public int getVertexBufferSizePerVertex(){return 16;}
    @Override public int getVertexCountPerBlock(){return 4;}
    @Override public int getIndexCountPerBlock(){return 8;}
    @Override public String getNameKey() {
        return "minihudStyle";
    }
    @Override public RenderPipeline getShader(boolean xray) {
        if(xray) return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_DEPTH_NO_CULL;
        else return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_CULL;
    }
    @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int index, boolean xray) {
        double yOffset = xray ? 0 : 0.005;
        float y = (float) (pos.getY() + yOffset);
        float minX = (float) (pos.getX() + 0.1), maxX = (float) (pos.getX() + 0.9);
        float minZ = (float) (pos.getZ() + 0.1), maxZ = (float) (pos.getZ() + 0.9);
        indexBuffer.putInt(index).putInt(index + 1);
        indexBuffer.putInt(index + 1).putInt(index + 2);
        indexBuffer.putInt(index + 2).putInt(index + 3);
        indexBuffer.putInt(index + 3).putInt(index);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ).putInt(0xffffffff);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ).putInt(0xffffffff);
        vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ).putInt(0xffffffff);
        vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ).putInt(0xffffffff);
    }
}
