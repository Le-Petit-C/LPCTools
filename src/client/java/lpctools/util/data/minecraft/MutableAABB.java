package lpctools.util.data.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.OptionalDouble;

@SuppressWarnings("unused")
public class MutableAABB {
	private double minX, minY, minZ, maxX, maxY, maxZ;
	public MutableAABB() {}
	public MutableAABB(double x1, double y1, double z1, double x2, double y2, double z2) { set(x1, y1, z1, x2, y2, z2); }
	@SuppressWarnings("CopyConstructorMissesField") public MutableAABB(MutableAABB aabb) { set(aabb); }
	public MutableAABB(AABB aabb) { set(aabb); }
	@Contract("_,_,_,_,_,_->this")
	private MutableAABB setUnchecked(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		return this;
	}
	@Contract("_,_,_,_,_,_->this")
	public MutableAABB set(double x1, double y1, double z1, double x2, double y2, double z2) {
		return setUnchecked(
			Math.min(x1, x2),
			Math.min(y1, y2),
			Math.min(z1, z2),
			Math.max(x1, x2),
			Math.max(y1, y2),
			Math.max(z1, z2)
		);
	}
	@Contract(pure = true) public double minX() { return minX; }
	@Contract(pure = true) public double minY() { return minY; }
	@Contract(pure = true) public double minZ() { return minZ; }
	@Contract(pure = true) public double maxX() { return maxX; }
	@Contract(pure = true) public double maxY() { return maxY; }
	@Contract(pure = true) public double maxZ() { return maxZ; }
	@Contract(pure = true) public double componentByDirection(Direction direction) {
		return switch (direction) {
			case DOWN -> minY;
			case UP -> maxY;
			case NORTH -> minZ;
			case SOUTH -> maxZ;
			case WEST -> minX;
			case EAST -> maxX;
		};
	}
	@Contract("_->this") public MutableAABB set(MutableAABB aabb) { return setUnchecked(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract("_->this") public MutableAABB set(AABB aabb) { return setUnchecked(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract("_,_,_->this") public MutableAABB moveAndSet(double x, double y, double z) { return setUnchecked(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z); }
	@Contract("_->this") public MutableAABB moveAndSet(Vec3 shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	@Contract("_->this") public MutableAABB moveAndSet(Vector3d shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	@Contract("_->this") public MutableAABB moveAndSet(Vector3f shift) { return moveAndSet(shift.x(), shift.y(), shift.z()); }
	@Contract("_->this") public MutableAABB moveAndSet(Vec3i shift) { return moveAndSet(shift.getX(), shift.getY(), shift.getZ()); }
	@Contract("_->this") public MutableAABB setFullCube(BlockPos pos) { return setUnchecked(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1); }
	@Contract("_,_,_->this") public MutableAABB inflateAndSet(double x, double y, double z) { return set(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z); }
	@Contract("_->this") public MutableAABB inflateAndSet(double amount) { return inflateAndSet(amount, amount, amount); }
	@Contract("_->param1") public <T extends Vector3d> T getCenter(T res) { res.set((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5); return res; }
	@Contract("->new") public Vec3 getCenter() { return new Vec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5); }
	@Contract(pure = true) public boolean intersects(MutableAABB aabb) { return intersects(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean intersects(AABB aabb) { return intersects(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean intersects(double x, double y, double z) { return rangeIntersects(x, minX, maxX) && rangeIntersects(y, minY, maxY) && rangeIntersects(z, minZ, maxZ); }
	@Contract(pure = true) public boolean intersects(Position pos) { return intersects(pos.x(), pos.y(), pos.z()); }
	@Contract(pure = true) public boolean intersects(Vector3dc pos) { return intersects(pos.x(), pos.y(), pos.z()); }
	@Contract(pure = true) public boolean touches(MutableAABB aabb) { return touches(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean touches(AABB aabb) { return touches(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean touches(double x, double y, double z) { return rangeTouches(x, minX, maxX) && rangeTouches(y, minY, maxY) && rangeTouches(z, minZ, maxZ); }
	@Contract(pure = true) public boolean touches(Position pos) { return touches(pos.x(), pos.y(), pos.z()); }
	@Contract(pure = true) public boolean touches(Vector3dc pos) { return touches(pos.x(), pos.y(), pos.z()); }
	@Contract(pure = true) public boolean contains(MutableAABB aabb) { return contains(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean contains(AABB aabb) { return contains(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }
	@Contract(pure = true) public boolean contains(double x, double y, double z) { return rangeContains(x, minX, maxX) && rangeContains(y, minY, maxY) && rangeContains(z, minZ, maxZ); }
	@Contract(pure = true) public boolean contains(Position pos) { return contains(pos.x(), pos.y(), pos.z()); }
	@Contract(pure = true) public boolean contains(Vector3dc pos) { return contains(pos.x(), pos.y(), pos.z()); }
	@Contract("_,_,_->this") public MutableAABB setCenter(double x, double y, double z) {
		return moveAndSet(x - (this.minX + this.maxX) * 0.5, y - (this.minY + this.maxY) * 0.5, z - (this.minZ + this.maxZ) * 0.5);
	}
	@Contract("_->this") public MutableAABB setCenter(Vec3 center) { return setCenter(center.x, center.y, center.z); }
	@Contract("_->this") public MutableAABB setCenter(Vector3d center) { return setCenter(center.x, center.y, center.z); }
	@Contract("_,_,_->this") public MutableAABB setBottomCenter(double x, double y, double z) {
		return moveAndSet(x - (this.minX + this.maxX) * 0.5, y - this.minY, z - (this.minZ + this.maxZ) * 0.5);
	}
	@Contract("_->this") public MutableAABB setBottomCenter(Vec3 center) { return setBottomCenter(center.x, center.y, center.z); }
	@Contract("_->this") public MutableAABB setBottomCenter(Vector3d center) { return setBottomCenter(center.x, center.y, center.z); }

	// 返回射线与AABB的最近交点系数（eye + result * ray = 交点坐标），eye在AABB内部时返回0，射线反方向时返回负值，不相交时返回OptionalDouble.empty()
	@Contract(pure = true) public OptionalDouble rayCastDistance(double eyeX, double eyeY, double eyeZ, double rayX, double rayY, double rayZ) {
		class Inner {
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
				return rangeTouches(eye, aabb1, aabb2);
			}
		}
		// Slab method: 对每个轴计算近远交点，取最近交点的最大值和最远交点的最小值
		double[] tnf = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
		if (Inner.slabAxis(tnf, eyeX, rayX, minX, maxX) && Inner.slabAxis(tnf, eyeY, rayY, minY, maxY) && Inner.slabAxis(tnf, eyeZ, rayZ, minZ, maxZ) && tnf[0] <= tnf[1])
			return OptionalDouble.of(Math.clamp(0, tnf[0], tnf[1]));
		else return OptionalDouble.empty();
	}

	@Contract(pure = true) public OptionalDouble rayCastDistance(Vec3 eye, Vec3 ray) { return rayCastDistance(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z); }
	@Contract(pure = true) public OptionalDouble rayCastDistance(Vector3d eye, Vec3 ray) { return rayCastDistance(eye.x, eye.y, eye.z, ray.x, ray.y, ray.z); }

	@Contract(value = "_->new", pure = true) public Vec3 clamp(Vec3 vec) { return new Vec3(Math.clamp(vec.x(), minX, maxX), Math.clamp(vec.y(), minY, maxY), Math.clamp(vec.z(), minZ, maxZ)); }

	@Contract("_,_->param2") public <T extends Vector3d> T clamp(Vector3dc vec, T res) {
		res.set(Math.clamp(vec.x(), minX, maxX), Math.clamp(vec.y(), minY, maxY), Math.clamp(vec.z(), minZ, maxZ));
		return res;
	}

	@Contract("_->param1") public <T extends Vector3d> T clamp(T vec) { return clamp(vec, vec); }

	@Contract("_,_->param2") public <T extends Vector3d> T closestToEdge(Vector3dc vec, T res) {
		double x = vec.x(), y = vec.y(), z = vec.z();
		double cx = Math.clamp(x, minX, maxX);
		double cy = Math.clamp(y, minY, maxY);
		double cz = Math.clamp(z, minZ, maxZ);
		if(x == cx && y == cy && z == cz) {
			// 点在 AABB 内部：投影到最近的面（最近的一面墙）
			double dX1 = x - minX, dX2 = maxX - x;
			double dY1 = y - minY, dY2 = maxY - y;
			double dZ1 = z - minZ, dZ2 = maxZ - z;
			double min = Math.min(Math.min(dX1, dX2), Math.min(Math.min(dY1, dY2), Math.min(dZ1, dZ2)));
			if(min == dX1) cx = minX;
			else if(min == dX2) cx = maxX;
			else if(min == dY1) cy = minY;
			else if(min == dY2) cy = maxY;
			else if(min == dZ1) cz = minZ;
			else cz = maxZ;
		}
		res.set(cx, cy, cz);
		return res;
	}

	@Contract(value = "_->param1") public <T extends Vector3d> T closestToEdge(T vec) { return closestToEdge(vec, vec); }

	@Contract(pure = true) private static boolean rangeIntersects(double min1, double max1, double min2, double max2) { return min1 < max2 && max1 > min2; }
	@Contract(pure = true) private static boolean rangeIntersects(double t, double min, double max) { return min < t && t < max; }
	@Contract(pure = true) private static boolean rangeTouches(double t, double min, double max) { return min <= t && t <= max; }
	@Contract(pure = true) private static boolean rangeTouches(double min1, double max1, double min2, double max2) { return min1 <= max2 && max1 >= min2; }
	@Contract(pure = true) private static boolean rangeContains(double t, double min, double max) { return min <= t && t < max; }
	@Contract(pure = true) private static boolean rangeContains(double min1, double max1, double min2, double max2) { return min1 <= min2 && max1 >= max2; }
	@Contract(pure = true) private boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return rangeIntersects(this.minX, this.maxX, minX, maxX) && rangeIntersects(this.minY, this.maxY, minY, maxY) && rangeIntersects(this.minZ, this.maxZ, minZ, maxZ);
	}
	@Contract(pure = true) private boolean touches(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return rangeTouches(this.minX, this.maxX, minX, maxX) && rangeTouches(this.minY, this.maxY, minY, maxY) && rangeTouches(this.minZ, this.maxZ, minZ, maxZ);
	}
	@Contract(pure = true) private boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return rangeContains(this.minX, this.maxX, minX, maxX) && rangeContains(this.minY, this.maxY, minY, maxY) && rangeContains(this.minZ, this.maxZ, minZ, maxZ);
	}
}
