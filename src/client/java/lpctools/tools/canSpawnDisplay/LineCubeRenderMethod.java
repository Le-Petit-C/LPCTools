package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import lpctools.lpcfymasaapi.render.LineCube;
import lpctools.lpcfymasaapi.render.PositionColorVertex;
import lpctools.lpcfymasaapi.render.Shape;
import lpctools.lpcfymasaapi.render.TranslucentShapes;
import net.minecraft.util.math.BlockPos;

public class LineCubeRenderMethod implements IRenderMethod {
	@Override public String getNameKey() { return "lineCube"; }
    @Override public Shape<PositionColorVertex> getShape(BlockPos pos, int color) { return new LineCube(pos, color); }
    @Override public RenderPipeline getPipeline() { return TranslucentShapes.linePipeline(); }
}
