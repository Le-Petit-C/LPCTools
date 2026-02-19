package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.Shape;
import net.minecraft.util.math.BlockPos;

//索引使用GL_UNSIGNED_BYTE
public interface IRenderMethod {
    String getNameKey();
    Shape<PositionColorVertex> getShape(BlockPos pos, int color);
    RenderPipeline getPipeline();
}
