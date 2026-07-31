package lpctools.util.data.minecraft;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Vector2fEx extends Vector2f {
	public Vector2fEx() { super(); }
	public Vector2fEx(float d) { super(d); }
	public Vector2fEx(float x, float y) { super(x, y); }
	public Vector2fEx(Vector2fc v) { super(v); }
	public Vector2fEx(Vector2ic v) { super(v); }
	public Vector2fEx(Vector2dc v) { super(v); }
	public Vector2fEx(Vector3fc v) { super(v); }
	public Vector2fEx(Vector3ic v) { super(v); }
	public Vector2fEx(Vector3dc v) { super(v); }
	public Vector2fEx(float[] xy) { super(xy); }
	public Vector2fEx(ByteBuffer buffer) { super(buffer); }
	public Vector2fEx(int index, ByteBuffer buffer) { super(index, buffer); }
	public Vector2fEx(FloatBuffer buffer) { super(buffer); }
	public Vector2fEx(int index, FloatBuffer buffer) { super(index, buffer); }
	public Vector2fEx(Vec3 v) { super((float) v.x, (float) v.y); }
	public Vector2fEx(Vec3i v) { super(v.getX(), v.getY()); }

	@Contract("->new") public Vec2 toMinecraftVec2() { return new Vec2(x, y); }

	@Contract("_->this") @Override public Vector2fEx set(double d) { super.set(d); return this; }
	@Contract("_,_->this") @Override public Vector2fEx set(double x, double y) { super.set(x, y); return this; }
	@Contract("_,_->this") @Override public Vector2fEx set(float x, float y) { super.set(x, y); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector2fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector2ic v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector2dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector3dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector3fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(Vector3ic v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2fEx set(float[] xy) { super.set(xy); return this; }
	@Contract("_->this") @Override public Vector2fEx set(ByteBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector2fEx set(int index, ByteBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_->this") @Override public Vector2fEx set(FloatBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector2fEx set(int index, FloatBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_,_->this") @Override public Vector2fEx setComponent(int component, float value) throws IllegalArgumentException { super.setComponent(component, value); return this; }
	@Contract("_->this") public Vector2fEx set(Vec2 v) { return set(v.x, v.y); }

	// add —— 自修改，返回 this
	@Contract("_->this") @Override public Vector2fEx add(Vector2fc v) { super.add(v); return this; }
	@Contract("_,_->this") @Override public Vector2fEx add(float x, float y) { super.add(x, y); return this; }

	// sub —— 自修改，返回 this
	@Contract("_->this") @Override public Vector2fEx sub(Vector2fc v) { super.sub(v); return this; }
	@Contract("_,_->this") @Override public Vector2fEx sub(float x, float y) { super.sub(x, y); return this; }

	// fma —— 自修改，返回 this
	@Contract("_,_->this") @Override public Vector2fEx fma(Vector2fc a, Vector2fc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") @Override public Vector2fEx fma(float a, Vector2fc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") public Vector2fEx fma(Vector2fc a, Vec2 b) { return fma(a.x(), a.y(), b.x, b.y, this); }
	@Contract("_,_->this") public Vector2fEx fma(float a, Vec2 b) { return fma(a, b.x, b.y, this); }

	// add —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public <T extends Vector2d> T add(Vector2dc v, T dest) { dest.set(this).add(v); return dest; }
	@Contract("_,_->param2") public Vector2fEx add(Vector2fc v, Vector2fEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2fEx add(float x, float y, Vector2fEx dest) { super.add(x, y, dest); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector2d> T add(double x, double y, T dest) { dest.set(this).add(x, y); return dest; }
	@Contract("_,_->param2") public <T extends Vector2d> T add(Vec2 v, T dest) { add(v.x, v.y, dest); return dest; }

	// sub —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public <T extends Vector2d> T sub(Vector2dc v, T dest) { dest.set(this).sub(v); return dest; }
	@Contract("_,_->param2") public Vector2fEx sub(Vector2fc v, Vector2fEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2fEx sub(float x, float y, Vector2fEx dest) { super.sub(x, y, dest); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector2d> T sub(double x, double y, T dest) { dest.set(this).sub(x, y); return dest; }
	@Contract("_,_->param2") public <T extends Vector2d> T sub(Vec2 v, T dest) { return sub(v.x, v.y, dest); }

	// fma —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_,_->param3") public Vector2fEx fma(Vector2fc a, Vector2fc b, Vector2fEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2fEx fma(float a, Vector2fc b, Vector2fEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public <T extends Vector2f> T fma(Vector2fc a, Vec2 b, T dest) { dest.set(this); return fma(a.x(), a.y(), b.x, b.y, dest); }
	@Contract("_,_,_->param3") public <T extends Vector2f> T fma(float a, Vec2 b, T dest) { return fma(a, b.x, b.y, dest); }
	@Contract("_,_,_->param3") public <T extends Vector2d> T fma(Vector2fc a, Vector2fc b, T dest) {
		dest.x = Math.fma(a.x(), b.x(), this.x);
		dest.y = Math.fma(a.y(), b.y(), this.y);
		return dest;
	}
	@Contract("_,_,_->param3") public <T extends Vector2d> T fma(Vector2fc a, Vector2dc b, T dest) {
		dest.x = Math.fma(a.x(), b.x(), this.x);
		dest.y = Math.fma(a.y(), b.y(), this.y);
		return dest;
	}
	@Contract("_,_,_->param3") public <T extends Vector2d> T fma(float a, Vector2dc b, T dest) {
		dest.x = Math.fma(a, b.x(), this.x);
		dest.y = Math.fma(a, b.y(), this.y);
		return dest;
	}
	@Contract("_,_,_,_->param4") public <T extends Vector2f> T fma(float k, float x, float y, T dest) {
		dest.x = Math.fma(k, x, this.x);
		dest.y = Math.fma(k, y, this.y);
		return dest;
	}
	@Contract("_,_,_,_,_->param5") public <T extends Vector2f> T fma(float kx, float ky, float x, float y, T dest) {
		dest.x = Math.fma(kx, x, this.x);
		dest.y = Math.fma(ky, y, this.y);
		return dest;
	}
}
