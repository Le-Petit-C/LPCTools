package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.Shape;
import net.minecraft.util.math.BlockPos;

//索引使用GL_UNSIGNED_BYTE
public interface IRenderMethod {
    String getNameKey();
    Shape<PositionColorVertex> getShape(BlockPos pos, int color, boolean xrays);
    RenderInstance getRenderInstance(boolean xrays);
}
