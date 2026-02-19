package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import lpctools.lpcfymasaapi.render.LineQuad;
import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.Shape;
import lpctools.lpcfymasaapi.render.TranslucentShapes;
import net.minecraft.util.math.BlockPos;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public String getNameKey() { return "minihudStyle"; }
    @Override public Shape<PositionColorVertex> getShape(BlockPos pos, int color) { return new LineQuad(pos, color); }
    @Override public RenderPipeline getPipeline() { return TranslucentShapes.linePipeline(); }
}
