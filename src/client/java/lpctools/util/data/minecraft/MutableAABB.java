package lpctools.util.data.minecraft;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

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
	public MutableAABB set(MutableAABB aabb) {
		return set(aabb.x1, aabb.y1, aabb.z1, aabb.x2, aabb.y2, aabb.z2);
	}
	public MutableAABB set(AABB aabb) {
		return set(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}
	public MutableAABB moveAndSet(double x, double y, double z) {
		return set(x1 + x, y1 + y, z1 + z, x2 + x, y2 + y, z2 + z);
	}
	public MutableAABB moveAndSet(Vec3 shift) {
		return moveAndSet(shift.x(), shift.y(), shift.z());
	}
	public MutableAABB moveAndSet(Vector3d shift) {
		return moveAndSet(shift.x(), shift.y(), shift.z());
	}
	public MutableAABB moveAndSet(Vector3f shift) {
		return moveAndSet(shift.x(), shift.y(), shift.z());
	}
	public MutableAABB moveAndSet(Vec3i shift) {
		return moveAndSet(shift.getX(), shift.getY(), shift.getZ());
	}
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

	// 返回值表示是否相交，若相交且resultStorage非null时表示交点乘数（可正可负，eye+resultStorage*ray=交点坐标，eye在AABB内部时为0）
	public boolean rayCastTest(double eyeX, double eyeY, double eyeZ, double rayX, double rayY, double rayZ, @Nullable MutableDouble resultStorage) {
		// Slab method: 对每个轴计算近远交点，取最近交点的最大值和最远交点的最小值
		double tNear = Double.NEGATIVE_INFINITY;
		double tFar = Double.POSITIVE_INFINITY;
		// X 轴
		if(rayX != 0) {
			double tx1 = (x1 - eyeX) / rayX;
			double tx2 = (x2 - eyeX) / rayX;
			tNear = Math.max(tNear, Math.min(tx1, tx2));
			tFar  = Math.min(tFar,  Math.max(tx1, tx2));
		} else if(touches(eyeX, x1, x2)) {
			return false;
		}
		// Y 轴
		if(rayY != 0) {
			double ty1 = (y1 - eyeY) / rayY;
			double ty2 = (y2 - eyeY) / rayY;
			tNear = Math.max(tNear, Math.min(ty1, ty2));
			tFar  = Math.min(tFar,  Math.max(ty1, ty2));
		} else if(touches(eyeY, y1, y2)) {
			return false;
		}
		// Z 轴
		if(rayZ != 0) {
			double tz1 = (z1 - eyeZ) / rayZ;
			double tz2 = (z2 - eyeZ) / rayZ;
			tNear = Math.max(tNear, Math.min(tz1, tz2));
			tFar  = Math.min(tFar,  Math.max(tz1, tz2));
		} else if(touches(eyeZ, z1, z2)) {
			return false;
		}
		if(tNear <= tFar) {
			if(resultStorage != null) resultStorage.setValue(Math.clamp(0, tNear, tFar));
			return true;
		}
		return false;
	}

	public boolean rayCastTest(Vec3 eye, Vec3 ray, @Nullable MutableDouble resultStorage) {
		return rayCastTest(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z, resultStorage);
	}

	public boolean rayCastTest(Vector3d eye, Vec3 ray, @Nullable MutableDouble resultStorage) {
		return rayCastTest(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z, resultStorage);
	}
}
