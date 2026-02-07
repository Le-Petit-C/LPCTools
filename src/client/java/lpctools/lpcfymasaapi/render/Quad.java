package lpctools.lpcfymasaapi.render;

import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;

// 平行四边形
public class Quad {
	public static final Quad EMPTY_QUAD = new Quad(new Vector3d(), new Vector3d(), new Vector3d(), 0);
	public Vector3d base, x, y;
	public int color;
	
	public Quad(Vector3d base, Vector3d x, Vector3d y, int color) {
		this.base = base;
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public Quad(Quad quad) {this(new Vector3d(quad.base), new Vector3d(quad.x), new Vector3d(quad.y), quad.color);}
	
	long getPackedCenterSectionPos() {
		double x = base.x + (this.x.x + this.y.x) * 0.5;
		double y = base.y + (this.x.y + this.y.y) * 0.5;
		double z = base.z + (this.x.z + this.y.z) * 0.5;
		return ChunkSectionPos.asLong(
			ChunkSectionPos.getSectionCoordFloored(x),
			ChunkSectionPos.getSectionCoordFloored(y),
			ChunkSectionPos.getSectionCoordFloored(z));
	}
	
	@Contract(value = "_->param1")
	Vector3d getCenterPos(Vector3d pos) {
		double x = base.x + (this.x.x + this.y.x) * 0.5;
		double y = base.y + (this.x.y + this.y.y) * 0.5;
		double z = base.z + (this.x.z + this.y.z) * 0.5;
		return pos.set(x, y, z);
	}
	
	@SuppressWarnings("UnusedReturnValue") @Contract("_->this")
	Quad set(Quad quad) {
		base.set(quad.base);
		x.set(quad.x);
		y.set(quad.y);
		color = quad.color;
		return this;
	}
}
