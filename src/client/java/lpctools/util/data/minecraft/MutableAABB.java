package lpctools.util.data.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.OptionalDouble;

import static lpctools.util.MathUtils.unorderedClamp;

@SuppressWarnings("unused")
public class MutableAABB {
	public double x1, y1, z1, x2, y2, z2;
	public MutableAABB() {}
	public MutableAABB(double x1, double y1, double z1, double x2, double y2, double z2) {
		set(x1, y1, z1, x2, y2, z2);
	}
	@SuppressWarnings("CopyConstructorMissesField") public MutableAABB(MutableAABB aabb) { set(aabb); }
	public MutableAABB(AABB aabb) { set(aabb); }
	public MutableAABB set(double x1, double y1, double z1, double x2, double y2, double z2) {
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		return this;
	}
	public MutableAABB sortAndSet() {
		double minX = Math.min(x1, x2);
		double maxX = Math.max(x1, x2);
		double minY = Math.min(y1, y2);
		double maxY = Math.max(y1, y2);
		double minZ = Math.min(z1, z2);
		double maxZ = Math.max(z1, z2);
		x1 = minX;
		y1 = minY;
		z1 = minZ;
		x2 = maxX;
		y2 = maxY;
		z2 = maxZ;
		return this;
	}
	public MutableAABB set(MutableAABB aabb) { return set(aabb.x1, aabb.y1, aabb.z1, aabb.x2, aabb.y2, aabb.z2); }
	public MutableAABB set(AABB aabb) { return set(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	public MutableAABB moveAndSet(double x, double y, double z) { return set(x1 + x, y1 + y, z1 + z, x2 + x, y2 + y, z2 + z); }
	public MutableAABB moveAndSet(Vec3 shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	public MutableAABB moveAndSet(Vector3d shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	public MutableAABB moveAndSet(Vector3f shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	public MutableAABB moveAndSet(Vec3i shift) { return moveAndSet(shift.getX(), shift.getY(), shift.getZ()); }
	public MutableAABB setFullCube(BlockPos pos) { return set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1); }
	public Vector3d getCenter(Vector3d res) { return res.set((x1 + x2) * 0.5, (y1 + y2) * 0.5, (z1 + z2) * 0.5); }
	public Vec3 getCenter() { return new Vec3((x1 + x2) * 0.5, (y1 + y2) * 0.5, (z1 + z2) * 0.5); }
	private static boolean intersects(double ax1, double ax2, double bx1, double bx2) {
		return Math.min(ax1, ax2) < Math.max(bx1, bx2) && Math.max(ax1, ax2) > Math.min(bx1, bx2);
	}
	private static boolean isInside(double t, double lim1, double lim2) {
		if(lim1 < lim2) return lim1 < t && t < lim2;
		else return lim2 < t && t < lim1;
	}
	private static boolean touches(double t, double lim1, double lim2) {
		if(lim1 < lim2) return lim1 <= t && t <= lim2;
		else return lim2 <= t && t <= lim1;
	}
	private static boolean touches(double ax1, double ax2, double bx1, double bx2) {
		return Math.min(ax1, ax2) <= Math.max(bx1, bx2) && Math.max(ax1, ax2) >= Math.min(bx1, bx2);
	}
	public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2) {
		return intersects(this.x1, this.x2, x1, x2) && intersects(this.y1, this.y2, y1, y2) && intersects(this.z1, this.z2, z1, z2);
	}
	public boolean intersects(MutableAABB aabb) {
		return intersects(aabb.x1, aabb.y1, aabb.z1, aabb.x2, aabb.y2, aabb.z2);
	}
	public boolean intersects(AABB aabb) {
		return intersects(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}
	public boolean touches(double x1, double y1, double z1, double x2, double y2, double z2) {
		return touches(this.x1, this.x2, x1, x2) && touches(this.y1, this.y2, y1, y2) && touches(this.z1, this.z2, z1, z2);
	}
	public boolean touches(MutableAABB aabb) { return touches(aabb.x1, aabb.y1, aabb.z1, aabb.x2, aabb.y2, aabb.z2); }
	public boolean touches(AABB aabb) { return touches(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	public MutableAABB setCenter(double x, double y, double z) {
		return moveAndSet(x - (this.x1 + this.x2) * 0.5, y - (this.y1 + this.y2) * 0.5, z - (this.z1 + this.z2) * 0.5);
	}
	public MutableAABB setCenter(Vec3 center) { return setCenter(center.x, center.y, center.z); }
	public MutableAABB setCenter(Vector3d center) { return setCenter(center.x, center.y, center.z); }
	public MutableAABB setBottomCenter(double x, double y, double z) {
		return moveAndSet(x - (this.x1 + this.x2) * 0.5, y - Math.min(this.y1, this.y2), z - (this.z1 + this.z2) * 0.5);
	}
	public MutableAABB setBottomCenter(Vec3 center) { return setBottomCenter(center.x, center.y, center.z); }
	public MutableAABB setBottomCenter(Vector3d center) { return setBottomCenter(center.x, center.y, center.z); }

	// 返回射线与AABB的最近交点系数（eye + result * ray = 交点坐标），eye在AABB内部时返回0，不相交时返回OptionalDouble.empty()
	public OptionalDouble rayCastDistance(double eyeX, double eyeY, double eyeZ, double rayX, double rayY, double rayZ) {
		// Slab method: 对每个轴计算近远交点，取最近交点的最大值和最远交点的最小值
		double[] tnf = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
		if (slabAxis(tnf, eyeX, rayX, x1, x2) && slabAxis(tnf, eyeY, rayY, y1, y2) && slabAxis(tnf, eyeZ, rayZ, z1, z2) && tnf[0] <= tnf[1])
			return OptionalDouble.of(Math.clamp(0, tnf[0], tnf[1]));
		else return OptionalDouble.empty();
	}

	// 对单个轴更新 tNearFar[0]=tNear, tNearFar[1]=tFar；返回 false 表示射线与该轴平行且不相交
	private static boolean slabAxis(double[] tnf, double eye, double ray, double aabb1, double aabb2) {
		if(ray != 0) {
			double t1 = (aabb1 - eye) / ray;
			double t2 = (aabb2 - eye) / ray;
			if(t1 < t2) {
				if(tnf[0] < t1) tnf[0] = t1;
				if(tnf[1] > t2) tnf[1] = t2;
			} else {
				if(tnf[0] < t2) tnf[0] = t2;
				if(tnf[1] > t1) tnf[1] = t1;
			}
			return true;
		}
		return touches(eye, aabb1, aabb2);
	}

	public OptionalDouble rayCastDistance(Vec3 eye, Vec3 ray) {
		return rayCastDistance(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z);
	}

	public OptionalDouble rayCastDistance(Vector3d eye, Vec3 ray) {
		return rayCastDistance(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z);
	}

	public <T extends Vector3d> T clamp(Vector3dc vec, T res) {
		res.set(unorderedClamp(vec.x(), x1, x2), unorderedClamp(vec.y(), y1, y2), unorderedClamp(vec.z(), z1, z2));
		return res;
	}

	public <T extends Vector3d> T clamp(T vec) { return clamp(vec, vec); }

	public <T extends Vector3d> T closestToEdge(Vector3dc vec, T res) {
		double x = vec.x(), y = vec.y(), z = vec.z();
		double cx = unorderedClamp(x, x1, x2);
		double cy = unorderedClamp(y, y1, y2);
		double cz = unorderedClamp(z, z1, z2);
		if(x == cx && y == cy && z == cz) {
			// 点在 AABB 内部：投影到最近的面（最近的一面墙）
			double loX = Math.min(x1, x2), hiX = Math.max(x1, x2);
			double loY = Math.min(y1, y2), hiY = Math.max(y1, y2);
			double loZ = Math.min(z1, z2), hiZ = Math.max(z1, z2);
			double dX1 = x - loX, dX2 = hiX - x;
			double dY1 = y - loY, dY2 = hiY - y;
			double dZ1 = z - loZ, dZ2 = hiZ - z;
			double min = Math.min(Math.min(dX1, dX2), Math.min(Math.min(dY1, dY2), Math.min(dZ1, dZ2)));
			if(min == dX1) cx = loX;
			else if(min == dX2) cx = hiX;
			else if(min == dY1) cy = loY;
			else if(min == dY2) cy = hiY;
			else if(min == dZ1) cz = loZ;
			else cz = hiZ;
		}
		res.set(cx, cy, cz);
		return res;
	}

	public <T extends Vector3d> T closestToEdge(T vec) { return closestToEdge(vec, vec); }
}
