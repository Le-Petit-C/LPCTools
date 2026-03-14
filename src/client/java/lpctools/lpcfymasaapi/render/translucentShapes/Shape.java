package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.IPositionVertex;
import lpctools.lpcfymasaapi.render.IVertex;
import lpctools.util.DataUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.function.Function;

public class Shape<T extends IVertex> {
	public final T[] vertices;
	public final int[][] baseIndices;
	public final Vector3d[] centers;
	public final Vector3d center;
	public Shape(T[] vertices, int[][] baseIndices, Vector3d[] centers, Vector3d center) {
		this.vertices = vertices;
		this.baseIndices = baseIndices;
		this.centers = centers;
		this.center = center;
		if(centers.length != baseIndices.length) throw new IllegalArgumentException();
	}
	
	public Shape(T[] vertices, int[][] baseIndices, Vector3d center) {
		this(vertices, baseIndices, new Vector3d[baseIndices.length], center);
	}
	
	protected static <T extends Shape<? extends IPositionVertex>> ShapeRegister<T>
	genShapeRegister(RenderInstance renderInstance) {
		return new ShapeRegister<>(renderInstance);
	}
	
	@SuppressWarnings("unused") protected void setDefaultCenters(Function<T, Vector3d> centerFunction) {
		for(int i = 0; i < baseIndices.length; i++){
			var indices = baseIndices[i];
			if(centers[i] == null) centers[i] = new Vector3d();
			else centers[i].set(0);
			for(int j : indices) centers[i].add(centerFunction.apply(vertices[j]));
			centers[i].div(indices.length);
		}
	}
	
	@SuppressWarnings("unused") public @Nullable Shape<T> removeUnusedVertices(){
		boolean[] used = new boolean[vertices.length];
		for(var v : baseIndices)
			for(var i : v)
				used[i] = true;
		int i = 0;
		int[] indexRemap = new int[vertices.length];
		for(int j = 0; j < vertices.length; ++j)
			if(used[j]) indexRemap[j] = i++;
		if(i == vertices.length) return this;
		else if(i == 0) return null;
		T[] newVertices = DataUtils.newArrayLike(vertices, i);
		for(int j = 0; j < vertices.length; ++j)
			if(used[j]) newVertices[j] = vertices[indexRemap[j]];
		int[][] newBaseIndices = new int[baseIndices.length][];
		for(int j = 0; j < newBaseIndices.length; ++j){
			var indices = baseIndices[j];
			int[] newIndices = new int[indices.length];
			for(int k = 0; k < indices.length; ++k)
				newIndices[k] = indexRemap[indices[k]];
			newBaseIndices[j] = newIndices;
		}
		return new Shape<>(newVertices, newBaseIndices, centers, center);
	}
}
