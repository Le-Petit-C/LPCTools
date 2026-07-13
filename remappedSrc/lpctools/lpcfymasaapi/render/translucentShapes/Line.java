package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.PositionColorVertex;
import org.joml.Vector3d;

public class Line extends PositionedShape<PositionColorVertex> {
	static final int[][] baseIndices = {{0, 1}};
	
	public Line(Vector3d pos0, int color0, Vector3d pos1, int color1) {
		super(new PositionColorVertex[2], baseIndices, new Vector3d());
		vertices[0] = new PositionColorVertex(pos0, color0);
		vertices[1] = new PositionColorVertex(pos1, color1);
		updateCenter();
	}
	
	public void updateCenter() {
		setDefaultCenters();
		center.set(centers[0]);
	}
	
	public Line() {
		this(new Vector3d(), 0, new Vector3d(), 0);
	}
	
	public static ShapeRegister<Line> register(boolean xrays) {
		return genShapeRegister(RenderInstance.defaultRenderInstance(true, xrays));
	}
}
