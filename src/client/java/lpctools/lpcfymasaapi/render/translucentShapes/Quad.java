package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.PositionColorVertex;
import org.joml.Vector3d;

// 平行四边形，逆时针方向定为base->base+u->base+u+v->base+v
public class Quad extends Shape<PositionColorVertex> {
	public static final int[][] cullIndices = {{0, 1, 2, 2, 1, 3}};
	public static final int[][] noCullIndices = {{0, 1, 2, 2, 1, 0, 2, 1, 3, 3, 1, 2}};
	public Vector3d base, u, v;
	public int color;
	
	public Quad(Vector3d base, Vector3d u, Vector3d v, int color, boolean cull) {
		super(new PositionColorVertex[4], cull ? cullIndices : noCullIndices, new Vector3d[1], new Vector3d());
		this.base = base;
		this.u = u;
		this.v = v;
		this.color = color;
		for(int i = 0; i < vertices.length; ++i) vertices[i] = new PositionColorVertex();
		update();
	}
	
	public Quad(Vector3d base, Vector3d u, Vector3d v, int color){
		this(base, u, v, color, true);
	}
	
	public void update(){
		centers[0] = center.set(u).add(v).mul(0.5).add(base);
		vertices[0].setPositionColor(base.x, base.y, base.z, color);
		vertices[1].setPositionColor(base.x + u.x, base.y + u.y, base.z + u.z, color);
		vertices[2].setPositionColor(base.x + v.x, base.y + v.y, base.z + v.z, color);
		vertices[3].setPositionColor(base.x + u.x + v.x, base.y + u.y + v.y, base.z + u.z + v.z, color);
	}
	
	public Quad(double baseX, double baseY, double baseZ, double uX, double uY, double uZ, double vX, double vY, double vZ, int color, boolean cull) {
		this(new Vector3d(baseX, baseY, baseZ), new Vector3d(uX, uY, uZ), new Vector3d(vX, vY, vZ), color, cull);
	}
	
	public Quad(double baseX, double baseY, double baseZ, double uX, double uY, double uZ, double vX, double vY, double vZ, int color) {
		this(new Vector3d(baseX, baseY, baseZ), new Vector3d(uX, uY, uZ), new Vector3d(vX, vY, vZ), color);
	}
	
	public Quad(Quad quad) {this(new Vector3d(quad.base), new Vector3d(quad.u), new Vector3d(quad.v), quad.color);}
}
