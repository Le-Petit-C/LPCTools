package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.IPositionVertex;
import org.joml.Vector3d;

public class PositionedShape<T extends IPositionVertex> extends Shape<T> {
	@SuppressWarnings("unused")
	public PositionedShape(T[] vertices, int[][] baseIndices, Vector3d[] centers, Vector3d center) {
		super(vertices, baseIndices, centers, center);
	}
	public PositionedShape(T[] vertices, int[][] baseIndices, Vector3d center) {
		super(vertices, baseIndices, center);
	}
	protected void setDefaultCenters() {
		for(int i = 0; i < baseIndices.length; i++){
			var indices = baseIndices[i];
			if(centers[i] == null) centers[i] = new Vector3d();
			else centers[i].set(0);
			for(int j : indices) {
				var vertex = vertices[j];
				centers[i].add(vertex.getX(), vertex.getY(), vertex.getZ());
			}
			centers[i].div(indices.length);
		}
	}
}
