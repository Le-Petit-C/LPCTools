package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.LineCube;
import lpctools.lpcfymasaapi.render.PositionColorLineWidthVertex;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.Shape;
import net.minecraft.util.math.BlockPos;

public class LineCubeRenderMethod implements IRenderMethod {
	@Override public String getNameKey() { return "lineCube"; }
    @Override public Shape<PositionColorLineWidthVertex> getShape(BlockPos pos, int color, boolean xrays) { return new LineCube(pos, color); }
    @Override public RenderInstance getRenderInstance(boolean xrays) { return RenderInstance.defaultRenderInstance(true, xrays); }
}
