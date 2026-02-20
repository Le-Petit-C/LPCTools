package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.LineQuad;
import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.Shape;
import net.minecraft.util.math.BlockPos;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public String getNameKey() { return "minihudStyle"; }
    @Override public Shape<PositionColorVertex> getShape(BlockPos pos, int color, boolean xrays) { return new LineQuad(pos, color, xrays); }
    @Override public RenderInstance getRenderInstance(boolean xrays) { return RenderInstance.defaultRenderInstance(true, xrays); }
}
