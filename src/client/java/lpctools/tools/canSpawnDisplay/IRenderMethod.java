package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.IPositionVertex;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.Shape;
import net.minecraft.util.math.BlockPos;

//索引使用GL_UNSIGNED_BYTE
public interface IRenderMethod {
    String getNameKey();
    Shape<? extends IPositionVertex> getShape(BlockPos pos, int color, boolean xrays);
    RenderInstance getRenderInstance(boolean xrays);
}
