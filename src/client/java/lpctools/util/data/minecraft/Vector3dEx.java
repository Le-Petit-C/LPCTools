package lpctools.util.data.minecraft;

import lpctools.util.MathUtils;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class Vector3dEx extends Vector3d implements Position {
	public Vector3dEx() { super(); }
	public Vector3dEx(double d) { super(d); }
	public Vector3dEx(double x, double y, double z) { super(x, y, z); }
	public Vector3dEx(Vector3fc v) { super(v); }
	public Vector3dEx(Vector3ic v) { super(v); }
	public Vector3dEx(Vector2fc v, double z) { super(v, z); }
	public Vector3dEx(Vector2ic v, double z) { super(v, z); }
	public Vector3dEx(Vector3dc v) { super(v); }
	public Vector3dEx(Vector2dc v, double z) { super(v, z); }
	public Vector3dEx(double[] xyz) { super(xyz); }
	public Vector3dEx(float[] xyz) { super(xyz); }
	public Vector3dEx(ByteBuffer buffer) { super(buffer); }
	public Vector3dEx(int index, ByteBuffer buffer) { super(index, buffer); }
	public Vector3dEx(DoubleBuffer buffer) { super(buffer); }
	public Vector3dEx(int index, DoubleBuffer buffer) { super(index, buffer); }
	public Vector3dEx(Position v) { super(v.x(), v.y(), v.z()); }
	public Vector3dEx(Vec3i v) { super(v.getX(), v.getY(), v.getZ()); }

	@Contract("->new") public Vec3 toMinecraftVec3() { return new Vec3(x, y, z); }

	@Contract("_->this") @Override public Vector3dEx set(double d) { super.set(d); return this; }
	@Contract("_,_,_->this") @Override public Vector3dEx set(double x, double y, double z) { super.set(x, y, z); return this; }
	@Contract("_->this") @Override public Vector3dEx set(Vector3dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector3dEx set(Vector3fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector3dEx set(Vector3ic v) { super.set(v); return this; }
	@Contract("_,_->this") @Override public Vector3dEx set(Vector2fc v, double z) { super.set(v, z); return this; }
	@Contract("_,_->this") @Override public Vector3dEx set(Vector2ic v, double z) { super.set(v, z); return this; }
	@Contract("_,_->this") @Override public Vector3dEx set(Vector2dc v, double z) { super.set(v, z); return this; }
	@Contract("_->this") @Override public Vector3dEx set(double[] xyz) { super.set(xyz); return this; }
	@Contract("_->this") @Override public Vector3dEx set(float[] xyz) { super.set(xyz); return this; }
	@Contract("_->this") @Override public Vector3dEx set(ByteBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector3dEx set(int index, ByteBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_->this") @Override public Vector3dEx set(DoubleBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector3dEx set(int index, DoubleBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_,_->this") @Override public Vector3dEx setComponent(int component, double value) throws IllegalArgumentException { super.setComponent(component, value); return this; }
	@Contract("_->this") public Vector3dEx set(Position v) { return set(v.x(), v.y(), v.z()); }
	@Contract("_->this") public Vector3dEx set(Vec3i v) { return set(v.getX(), v.getY(), v.getZ()); }
	@Contract("_->this") public Vector3dEx setAsCenter(Vec3i v) { return set(v.getX() + 0.5, v.getY() + 0.5, v.getZ() + 0.5); }

	// add —— 自修改，返回 this
	@Contract("_->this") @Override public Vector3dEx add(Vector3dc v) { super.add(v); return this; }
	@Contract("_->this") @Override public Vector3dEx add(Vector3fc v) { super.add(v); return this; }
	@Contract("_,_,_->this") @Override public Vector3dEx add(double x, double y, double z) { super.add(x, y, z); return this; }
	@Contract("_->this") public Vector3dEx add(Position v) { return add(v.x(), v.y(), v.z()); }
	@Contract("_->this") public Vector3dEx add(Vec3i v) { return add(v.getX(), v.getY(), v.getZ()); }

	// sub —— 自修改，返回 this
	@Contract("_->this") @Override public Vector3dEx sub(Vector3dc v) { super.sub(v); return this; }
	@Contract("_->this") @Override public Vector3dEx sub(Vector3fc v) { super.sub(v); return this; }
	@Contract("_,_,_->this") @Override public Vector3dEx sub(double x, double y, double z) { super.sub(x, y, z); return this; }
	@Contract("_->this") public Vector3dEx sub(Position v) { return sub(v.x(), v.y(), v.z()); }
	@Contract("_->this") public Vector3dEx sub(Vec3i v) { return sub(v.getX(), v.getY(), v.getZ()); }

	// fma —— 自修改，返回 this
	@Contract("_,_->this") @Override public Vector3dEx fma(Vector3dc a, Vector3dc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") @Override public Vector3dEx fma(double a, Vector3dc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") public Vector3dEx fma(Vector3dc a, Position b) { return fma(a, b, this); }
	@Contract("_,_->this") public Vector3dEx fma(double a, Position b) { return fma(a, b, this); }

	// add —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public Vector3dEx add(Vector3dc v, Vector3dEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_->param2") public Vector3dEx add(Vector3fc v, Vector3dEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_,_,_->param4") public Vector3dEx add(double x, double y, double z, Vector3dEx dest) { super.add(x, y, z, dest); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T add(Position v, T dest) { dest.set(this).add(v.x(), v.y(), v.z()); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T add(Vec3i v, T dest) { dest.set(this).add(v.getX(), v.getY(), v.getZ()); return dest; }


	// sub —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public Vector3dEx sub(Vector3dc v, Vector3dEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_->param2") public Vector3dEx sub(Vector3fc v, Vector3dEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_,_,_->param4") public Vector3dEx sub(double x, double y, double z, Vector3dEx dest) { super.sub(x, y, z, dest); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T sub(Position v, T dest) { dest.set(this).sub(v.x(), v.y(), v.z()); return dest; }
	@Contract("_,_->param2") public <T extends Vector3d> T sub(Vec3i v, T dest) { dest.set(this).sub(v.getX(), v.getY(), v.getZ()); return dest; }
	@Contract("_,_->param2") public <T extends Vector3f> T sub(Vector3dc v, T dest) { dest.set(x - v.x(), y - v.y(), z - v.z()); return dest; }
	@Contract("_,_->param2") public <T extends Vector3f> T sub(Position v, T dest) { dest.set(x - v.x(), y - v.y(), z - v.z()); return dest; }

	// fma —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_,_->param3") public Vector3dEx fma(Vector3dc a, Vector3dc b, Vector3dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector3dEx fma(double a, Vector3dc b, Vector3dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector3dEx fma(Vector3fc a, Vector3fc b, Vector3dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector3dEx fma(Vector3dc a, Vector3fc b, Vector3dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector3dEx fma(double a, Vector3fc b, Vector3dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(Vector3dc a, Position b, T dest) { return fma(a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), dest); }
	@Contract("_,_,_->param3") public <T extends Vector3d> T fma(double a, Position b, T dest) { return fma(a, b.x(), b.y(), b.z(), dest); }

	@Contract(pure = true) public double distance(Position v) { return distance(v.x(), v.y(), v.z()); }
	@Contract(pure = true) public double distance(Vec3i v) { return distance(v.getX(), v.getY(), v.getZ()); }
	@Contract(pure = true) public double distanceSquared(Position v) { return distanceSquared(v.x(), v.y(), v.z()); }
	@Contract(pure = true) public double distanceSquared(Vec3i v) { return distanceSquared(v.getX(), v.getY(), v.getZ()); }

	@Contract(pure = true) public double XRotOrDefault(double def) { return MathUtils.XRotOrDefault(x, y, z, def); }
	@Contract(pure = true) public double YRotOrDefault(double def) { return MathUtils.YRotOrDefault(x, y, z, def); }
	@Contract("_,_,_->param3") public <T extends Vector2d> T XYRotOrDef(double xDef, double yDef, T res) { return MathUtils.XYRotOrDef(x, y, z, xDef, yDef, res); }
	@Contract("_,_->param2") public <T extends Vector2d> T XYRotOrDef(Vector2dc def, T res) { return MathUtils.XYRotOrDef(x, y, z, def.x(), def.y(), res); }
	@Contract("_->param1") public <T extends Vector2d> T XYRotOrDef(T v) { return MathUtils.XYRotOrDef(x, y, z, v.x(), v.y(), v); }
	@Contract("_,_->new") public Vector2dEx XYRotOrDef(double xDef, double yDef) { return MathUtils.XYRotOrDef(x, y, z, xDef, yDef, new Vector2dEx()); }
	@Contract("_,_->this") public Vector3dEx fromXYRot(double xRot, double yRot) { return MathUtils.viewVecFromXYRot(xRot, yRot, this); }
	@Contract("_->this") public Vector3dEx fromXYRot(Vector2dc v) { return MathUtils.viewVecFromXYRot(v.x(), v.y(), this); }

	@Contract("_,_,_,_,_->param5") public <T extends Vector3d> T fma(double k, double x, double y, double z, T dest) {
		dest.x = Math.fma(k, x, this.x);
		dest.y = Math.fma(k, y, this.y);
		dest.z = Math.fma(k, z, this.z);
		return dest;
	}

	@Contract("_,_,_,_,_,_,_->param7") public <T extends Vector3d> T fma(double kx, double ky, double kz, double x, double y, double z, T dest) {
		dest.x = Math.fma(kx, x, this.x);
		dest.y = Math.fma(ky, y, this.y);
		dest.z = Math.fma(kz, z, this.z);
		return dest;
	}
}
