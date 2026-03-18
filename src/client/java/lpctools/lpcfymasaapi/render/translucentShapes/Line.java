package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.PositionColorLineWidthVertex;
import org.joml.Vector3d;

public class Line extends PositionedShape<PositionColorLineWidthVertex> {
	static final int[][] baseIndices = {{0, 1}};
	
	public Line(Vector3d pos0, int color0, float lineWidth0, Vector3d pos1, int color1, float lineWidth1) {
		super(new PositionColorLineWidthVertex[2], baseIndices, new Vector3d());
		vertices[0] = new PositionColorLineWidthVertex(pos0, color0, lineWidth0);
		vertices[1] = new PositionColorLineWidthVertex(pos1, color1, lineWidth1);
		updateCenter();
	}
	
	public void updateCenter() {
		setDefaultCenters();
		center.set(centers[0]);
	}
	
	public Line() {
		this(new Vector3d(), 0, 1, new Vector3d(), 0, 1);
	}
	
	public static ShapeRegister<Line> register(boolean xrays) {
		return genShapeRegister(RenderInstance.defaultRenderInstance(true, xrays));
	}
}
