package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.PositionColorVertex;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public class LineQuad extends Shape<PositionColorVertex> {
	static final int[][] baseIndices = {{0, 1}, {2, 3}, {0, 2}, {1, 3},};
	
	public LineQuad(BlockPos pos, int color, boolean xrays) {
		super(new PositionColorVertex[4], baseIndices, new Vector3d());
		center.set(pos.getX() + 0.5, pos.getY() + (xrays ? 0.0 : 0.005), pos.getZ() + 0.5);
		for (int i = 0; i < 4; ++i) {
			vertices[i] = new PositionColorVertex(
				pos.getX() + ((i & 1) == 0 ? 0.1 : 0.9),
				pos.getY() + 0.005,
				pos.getZ() + ((i & 2) == 0 ? 0.1 : 0.9),
				color);
		}
		setDefaultCenters(this);
	}
}
