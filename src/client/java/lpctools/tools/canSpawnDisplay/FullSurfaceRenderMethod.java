package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.translucentShapes.*;
import net.minecraft.util.math.BlockPos;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public String getNameKey() {return "fullSurface";}
    @Override public Shape<PositionColorVertex> getShape(BlockPos pos, int color, boolean xrays) {
        return new Quad(pos.getX(), pos.getY(), pos.getZ(), 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, color, false);
    }
    @Override public RenderInstance getRenderInstance(boolean xrays) {return RenderInstance.defaultRenderInstance(false, xrays);}
}
