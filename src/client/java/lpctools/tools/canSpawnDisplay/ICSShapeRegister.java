package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import net.minecraft.util.math.BlockPos;

public interface ICSShapeRegister {
	ShapeReference registerShape(BlockPos pos, int color);
}
