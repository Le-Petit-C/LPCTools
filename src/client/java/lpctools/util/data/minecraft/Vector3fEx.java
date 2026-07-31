package lpctools.util.data.minecraft;

import lpctools.util.MathUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Vector3fEx extends Vector3f {
	public Vector3fEx() { super(); }
	public Vector3fEx(float d) { super(d); }
	public Vector3fEx(float x, float y, float z) { super(x, y, z); }
	public Vector3fEx(Vector3fc v) { super(v); }
	public Vector3fEx(Vector3ic v) { super(v); }
	public Vector3fEx(Vector2fc v, float z) { super(v, z); }
	public Vector3fEx(Vector2ic v, float z) { super(v, z); }
	public Vector3fEx(Vector3dc v) { super(v); }
	public Vector3fEx(float[] xyz) { super(xyz); }
	public Vector3fEx(ByteBuffer buffer) { super(buffer); }
	public Vector3fEx(int index, ByteBuffer buffer) { super(index, buffer); }
	public Vector3fEx(FloatBuffer buffer) { super(buffer); }
	public Vector3fEx(int index, FloatBuffer buffer) { super(index, buffer); }
	public Vector3fEx(Vec3 v) { super((float) v.x, (float) v.y, (float) v.z); }
	public Vector3fEx(Vec3i v) { super(v.getX(), v.getY(), v.getZ()); }

	@Contract("_->this") @Override public Vector3fEx set(double d) { super.set(d); return this; }
	@Contract("_,_,_->this") @Override public Vector3fEx set(double x, double y, double z) { super.set(x, y, z); return this; }
	@Contract("_,_,_->this") @Override public Vector3fEx set(float x, float y, float z) { super.set(x, y, z); return this; }
	@Contract("_->this") @Override public Vector3fEx set(Vector3dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector3fEx set(Vector3fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector3fEx set(Vector3ic v) { super.set(v); return this; }
	@Contract("_,_->this") @Override public Vector3fEx set(Vector2fc v, float z) { super.set(v, z); return this; }
	@Contract("_,_->this") @Override public Vector3fEx set(Vector2ic v, float z) { super.set(v, z); return this; }
	@Contract("_,_->this") @Override public Vector3fEx set(Vector2dc v, float z) { super.set(v, z); return this; }
	@Contract("_->this") @Override public Vector3fEx set(float[] xyz) { super.set(xyz); return this; }
	@Contract("_->this") @Override public Vector3fEx set(ByteBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector3fEx set(int index, ByteBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_->this") @Override public Vector3fEx set(FloatBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector3fEx set(int index, FloatBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_,_->this") @Override public Vector3fEx setComponent(int component, float value) throws IllegalArgumentException { super.setComponent(component, value); return this; }
	@Contract("_->this") public Vector3fEx set(Vec3 v) { return set(v.x, v.y, v.z); }
	@Contract("_->this") public Vector3fEx set(Vec3i v) { return set(v.getX(), v.getY(), v.getZ()); }
	@Contract("_->this") public Vector3fEx setAsCenter(Vec3i v) { return set(v.getX() + 0.5, v.getY() + 0.5, v.getZ() + 0.5); }

	// add —— 自修改，返回 this
	@Contract("_->this") @Override public Vector3fEx add(Vector3fc v) { super.add(v); return this; }
	@Contract("_,_,_->this") @Override public Vector3fEx add(float x, float y, float z) { super.add(x, y, z); return this; }
	@Contract("_->this") public Vector3fEx add(Vec3i v) { return add(v.getX(), v.getY(), v.getZ()); }

	// sub —— 自修改，返回 this
	@Contract("_->this") @Override public Vector3fEx sub(Vector3fc v) { super.sub(v); return this; }
	@Contract("_,_,_->this") @Override public Vector3fEx sub(float x, float y, float z) { super.sub(x, y, z); return this; }
	@Contract("_->this") public Vector3fEx sub(Vec3i v) { return sub(v.getX(), v.getY(), v.getZ()); }

	// fma —— 自修改，返回 this
	@Contract("_,_->this") @Override public Vector3fEx fma(Vector3fc a, Vector3fc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") @Override public Vector3fEx fma(float a, Vector3fc b) { super.fma(a, b); return this; }

	// add —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public <T extends Vector3d> T add(Vector3dc v, T dest) { dest.set(this).add(v); return dest; }
	@Contract("_,_->param2") public Vector3fEx add(Vector3fc v, Vector3fEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_,_,_->param4") public Vector3fEx add(float x, float y, float z, Vector3fEx dest) { super.add(x, y, z, dest); return dest; }
	@Contract("_,_,_,_->param4") public <T extends Vector3d> T add(double x, double y, double z, T dest) { dest.set(this).add(x, y, z); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T add(Vec3 v, T dest) { return add(v.x, v.y, v.z, dest); }
	@Contract("_,_->param2") public <T extends Vector3f> T add(Vec3i v, T dest) { add(v.getX(), v.getY(), v.getZ(), dest); return dest; }

	// sub —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public <T extends Vector3d> T sub(Vector3dc v, T dest) { dest.set(this).sub(v); return dest; }
	@Contract("_,_->param2") public Vector3fEx sub(Vector3fc v, Vector3fEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_,_,_->param4") public Vector3fEx sub(float x, float y, float z, Vector3fEx dest) { super.sub(x, y, z, dest); return dest; }
	@Contract("_,_,_,_->param4") public <T extends Vector3d> T sub(double x, double y, double z, T dest) { dest.set(this).sub(x, y, z); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T sub(Vec3 v, T dest) { return sub(v.x, v.y, v.z, dest); }
	@Contract("_,_->param2") public <T extends Vector3f> T sub(Vec3i v, T dest) { sub(v.getX(), v.getY(), v.getZ(), dest); return dest; }

	// fma —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_,_->param3") public Vector3fEx fma(Vector3fc a, Vector3fc b, Vector3fEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector3fEx fma(float a, Vector3fc b, Vector3fEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(Vector3fc a, Vector3fc b, T dest) { dest.set(this).fma(a, b); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(Vector3fc a, Vector3dc b, T dest) {
		dest.x = Math.fma(a.x(), b.x(), this.x);
		dest.y = Math.fma(a.y(), b.y(), this.y);
		dest.z = Math.fma(a.z(), b.z(), this.z);
		return dest;
	}
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(float a, Vector3dc b, T dest) {
		dest.x = Math.fma(a, (float)b.x(), this.x);
		dest.y = Math.fma(a, (float)b.y(), this.y);
		dest.z = Math.fma(a, (float)b.z(), this.z);
		return dest;
	}
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(Vector3fc a, Vec3 b, T dest) {
		dest.x = Math.fma(a.x(), b.x, this.x);
		dest.y = Math.fma(a.y(), b.y, this.y);
		dest.z = Math.fma(a.z(), b.z, this.z);
		return dest;
	}
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(float a, Vec3 b, T dest) {
		dest.x = Math.fma(a, (float)b.x, this.x);
		dest.y = Math.fma(a, (float)b.y, this.y);
		dest.z = Math.fma(a, (float)b.z, this.z);
		return dest;
	}
	@Contract("_,_,_,_,_->param5") public <T extends Vector3f> T fma(float k, float x, float y, float z, T dest) {
		dest.x = Math.fma(k, x, this.x);
		dest.y = Math.fma(k, y, this.y);
		dest.z = Math.fma(k, z, this.z);
		return dest;
	}
	@Contract("_,_,_,_,_,_,_->param7") public <T extends Vector3f> T fma(float kx, float ky, float kz, float x, float y, float z, T dest) {
		dest.x = Math.fma(kx, x, this.x);
		dest.y = Math.fma(ky, y, this.y);
		dest.z = Math.fma(kz, z, this.z);
		return dest;
	}

	@Contract(pure = true) public float distance(Vec3i v) { return distance(v.getX(), v.getY(), v.getZ()); }
	@Contract(pure = true) public float distanceSquared(Vec3i v) { return distanceSquared(v.getX(), v.getY(), v.getZ()); }

	@Contract(pure = true) public float XRotOrDefault(float def) { return MathUtils.XRotOrDefault(x, y, z, def); }
	@Contract(pure = true) public float YRotOrDefault(float def) { return MathUtils.YRotOrDefault(x, y, z, def); }
	@Contract("_,_,_->param3") public <T extends Vector2f> T XYRotOrDef(float xDef, float yDef, T res) { return MathUtils.XYRotOrDef(x, y, z, xDef, yDef, res); }
	@Contract("_,_->param2") public <T extends Vector2f> T XYRotOrDef(Vector2fc def, T res) { return MathUtils.XYRotOrDef(x, y, z, def.x(), def.y(), res); }
	@Contract("_->param1") public <T extends Vector2f> T XYRotOrDef(T v) { return MathUtils.XYRotOrDef(x, y, z, v.x(), v.y(), v); }
	@Contract("_,_->new") public Vector2fEx XYRotOrDef(float xDef, float yDef) { return MathUtils.XYRotOrDef(x, y, z, xDef, yDef, new Vector2fEx()); }
	@Contract("_,_->this") public Vector3fEx fromXYRot(float xRot, float yRot) { return MathUtils.viewVecFromXYRot(xRot, yRot, this); }
	@Contract("_->this") public Vector3fEx fromXYRot(Vector2fc v) { return MathUtils.viewVecFromXYRot(v.x(), v.y(), this); }
}
