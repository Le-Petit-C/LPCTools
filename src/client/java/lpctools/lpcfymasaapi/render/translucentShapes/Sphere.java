package lpctools.lpcfymasaapi.render.translucentShapes;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.lpcfymasaapi.render.PositionColorLineWidthVertex;
import org.joml.Vector3d;

public class Sphere extends PositionedShape<PositionColorLineWidthVertex> {
	private static final int[][] baseIndices = {
		{0, 1, 3, 0, 1, 3, 0, 3, 2, 0, 3, 2},
		{0, 5, 1, 0, 5, 1, 0, 4, 5, 0, 4, 5},
		{0, 6, 4, 0, 6, 4, 0, 2, 6, 0, 2, 6},
		{4, 7, 5, 4, 7, 5, 4, 6, 7, 4, 6, 7},
		{2, 3, 7, 2, 3, 7, 2, 7, 6, 2, 7, 6},
		{1, 5, 7, 1, 5, 7, 1, 7, 3, 1, 7, 3}
	};
	public Sphere(Vector3d center, int color, float radius) {
		super(new PositionColorLineWidthVertex[8], baseIndices, center);
		for(int i = 0; i < 8; ++i) vertices[i] = new PositionColorLineWidthVertex(center, color, radius);
		centers[0] = new Vector3d(center).add( 0, 0,-radius);
		centers[1] = new Vector3d(center).add( 0,-radius, 0);
		centers[2] = new Vector3d(center).add(-radius, 0, 0);
		centers[3] = new Vector3d(center).add(0, 0, radius);
		centers[4] = new Vector3d(center).add(0, radius, 0);
		centers[5] = new Vector3d(center).add(radius, 0, 0);
	}
	
	public static ShapeRegister<Sphere> register(boolean xrays) {
		return new ShapeRegister<>(RenderInstance.getRenderInstance(new RenderOption(
			LPCRenderPipelines.spherePipeline, true, !xrays, TranslateMethod.PROJECTION__MODEL_VIEW__BIASED_OFFSET,
			xrays ? RenderTiming.END_MAIN : RenderTiming.BEFORE_TRANSLUCENT,
			ImmutableSet.of()
		)));
	}
}
