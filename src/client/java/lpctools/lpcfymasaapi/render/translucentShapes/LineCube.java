package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.PositionColorLineWidthVertex;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public class LineCube extends Shape<PositionColorLineWidthVertex> {
	static final int[][] baseIndices = {
		{0, 1}, {2, 3}, {4, 5}, {6, 7},
		{0, 2}, {1, 3}, {4, 6}, {5, 7},
		{0, 4}, {1, 5}, {2, 6}, {3, 7}
	};
	
	public LineCube(BlockPos pos, int color, float lineWidth) {
		super(new PositionColorLineWidthVertex[8], baseIndices, new Vector3d());
		center.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		for (int i = 0; i < 8; ++i) {
			vertices[i] = new PositionColorLineWidthVertex(
				pos.getX() + ((i & 1) == 0 ? 0.1 : 0.9),
				pos.getY() + ((i & 2) == 0 ? 0.1 : 0.9),
				pos.getZ() + ((i & 4) == 0 ? 0.1 : 0.9),
				color, lineWidth);
		}
		setDefaultCenters(this);
	}
	public LineCube(BlockPos pos, int color){
		this(pos, color, 1.0f);
	}
}
