package lpctools.lpcfymasaapi.render;

import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;

// 平行四边形，方向定为base->base+u->base+u+v->base+v
public class Quad {
	public Vector3d base, u, v;
	public int color;
	
	public Quad(Vector3d base, Vector3d u, Vector3d v, int color) {
		this.base = base;
		this.u = u;
		this.v = v;
		this.color = color;
	}
	
	public Quad(double baseX, double baseY, double baseZ, double uX, double uY, double uZ, double vX, double vY, double vZ, int color) {
		this(new Vector3d(baseX, baseY, baseZ), new Vector3d(uX, uY, uZ), new Vector3d(vX, vY, vZ), color);
	}
	
	public Quad(Quad quad) {this(new Vector3d(quad.base), new Vector3d(quad.u), new Vector3d(quad.v), quad.color);}
	
	@SuppressWarnings("unused")
	long getPackedCenterSectionPos() {
		double x = base.x + (this.u.x + this.v.x) * 0.5;
		double y = base.y + (this.u.y + this.v.y) * 0.5;
		double z = base.z + (this.u.z + this.v.z) * 0.5;
		return ChunkSectionPos.asLong(
			ChunkSectionPos.getSectionCoordFloored(x),
			ChunkSectionPos.getSectionCoordFloored(y),
			ChunkSectionPos.getSectionCoordFloored(z));
	}
	
	@Contract(value = "_->param1")
	Vector3d getCenterPos(Vector3d pos) {
		double x = base.x + (this.u.x + this.v.x) * 0.5;
		double y = base.y + (this.u.y + this.v.y) * 0.5;
		double z = base.z + (this.u.z + this.v.z) * 0.5;
		return pos.set(x, y, z);
	}
	
	@SuppressWarnings({"UnusedReturnValue", "unused"}) @Contract("_->this")
	Quad set(Quad quad) {
		base.set(quad.base);
		u.set(quad.u);
		v.set(quad.v);
		color = quad.color;
		return this;
	}
}
