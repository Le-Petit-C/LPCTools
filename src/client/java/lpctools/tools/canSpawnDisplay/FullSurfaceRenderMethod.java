package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.Quad;
import lpctools.lpcfymasaapi.render.Shape;
import lpctools.lpcfymasaapi.render.TranslucentShapes;
import net.minecraft.util.math.BlockPos;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public String getNameKey() {return "fullSurface";}
    @Override public Shape<PositionColorVertex> getShape(BlockPos pos, int color) {
        return new Quad(pos.getX(), pos.getY() + 0.005, pos.getZ(), 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, color);
    }
    @Override public RenderPipeline getPipeline() {return TranslucentShapes.shapePipeline();}
}
