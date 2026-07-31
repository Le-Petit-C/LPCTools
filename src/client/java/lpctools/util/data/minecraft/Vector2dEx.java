package lpctools.util.data.minecraft;

import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class Vector2dEx extends Vector2d {
	public Vector2dEx() { super(); }
	public Vector2dEx(double d) { super(d); }
	public Vector2dEx(double x, double y) { super(x, y); }
	public Vector2dEx(Vector2dc v) { super(v); }
	public Vector2dEx(Vector2fc v) { super(v); }
	public Vector2dEx(Vector2ic v) { super(v); }
	public Vector2dEx(Vector3dc v) { super(v); }
	public Vector2dEx(Vector3fc v) { super(v); }
	public Vector2dEx(Vector3ic v) { super(v); }
	public Vector2dEx(double[] xy) { super(xy); }
	public Vector2dEx(float[] xy) { super(xy); }
	public Vector2dEx(ByteBuffer buffer) { super(buffer); }
	public Vector2dEx(int index, ByteBuffer buffer) { super(index, buffer); }
	public Vector2dEx(DoubleBuffer buffer) { super(buffer); }
	public Vector2dEx(int index, DoubleBuffer buffer) { super(index, buffer); }

	@Contract("_->this") @Override public Vector2dEx set(double d) { super.set(d); return this; }
	@Contract("_,_->this") @Override public Vector2dEx set(double x, double y) { super.set(x, y); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector2dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector2fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector2ic v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector3dc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector3fc v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(Vector3ic v) { super.set(v); return this; }
	@Contract("_->this") @Override public Vector2dEx set(double[] xy) { super.set(xy); return this; }
	@Contract("_->this") @Override public Vector2dEx set(float[] xy) { super.set(xy); return this; }
	@Contract("_->this") @Override public Vector2dEx set(ByteBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector2dEx set(int index, ByteBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_->this") @Override public Vector2dEx set(DoubleBuffer buffer) { super.set(buffer); return this; }
	@Contract("_,_->this") @Override public Vector2dEx set(int index, DoubleBuffer buffer) { super.set(index, buffer); return this; }
	@Contract("_,_->this") @Override public Vector2dEx setComponent(int component, double value) throws IllegalArgumentException { super.setComponent(component, value); return this; }

	// add —— 自修改，返回 this
	@Contract("_->this") @Override public Vector2dEx add(Vector2dc v) { super.add(v); return this; }
	@Contract("_->this") @Override public Vector2dEx add(Vector2fc v) { super.add(v); return this; }
	@Contract("_,_->this") @Override public Vector2dEx add(double x, double y) { super.add(x, y); return this; }

	// sub —— 自修改，返回 this
	@Contract("_->this") @Override public Vector2dEx sub(Vector2dc v) { super.sub(v); return this; }
	@Contract("_->this") @Override public Vector2dEx sub(Vector2fc v) { super.sub(v); return this; }
	@Contract("_,_->this") @Override public Vector2dEx sub(double x, double y) { super.sub(x, y); return this; }

	// fma —— 自修改，返回 this
	@Contract("_,_->this") @Override public Vector2dEx fma(Vector2dc a, Vector2dc b) { super.fma(a, b); return this; }
	@Contract("_,_->this") @Override public Vector2dEx fma(double a, Vector2dc b) { super.fma(a, b); return this; }

	// add —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public Vector2dEx add(Vector2dc v, Vector2dEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_->param2") public Vector2dEx add(Vector2fc v, Vector2dEx dest) { super.add(v, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2dEx add(double x, double y, Vector2dEx dest) { super.add(x, y, dest); return dest; }

	// sub —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_->param2") public Vector2dEx sub(Vector2dc v, Vector2dEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_->param2") public Vector2dEx sub(Vector2fc v, Vector2dEx dest) { super.sub(v, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2dEx sub(double x, double y, Vector2dEx dest) { super.sub(x, y, dest); return dest; }
	@Contract("_,_->param2") public <T extends Vector2f> T sub(Vector2dc v, T dest) { dest.set(x - v.x(), y - v.y()); return dest; }

	// fma —— 写入 dest，不修改 this，返回 dest
	@Contract("_,_,_->param3") public Vector2dEx fma(Vector2dc a, Vector2dc b, Vector2dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_->param3") public Vector2dEx fma(double a, Vector2dc b, Vector2dEx dest) { super.fma(a, b, dest); return dest; }
	@Contract("_,_,_,_->param4") public <T extends Vector2d> T fma(double k, double x, double y, T dest) {
		dest.x = Math.fma(k, x, this.x);
		dest.y = Math.fma(k, y, this.y);
		return dest;
	}
	@Contract("_,_,_,_,_->param5") public <T extends Vector2d> T fma(double kx, double ky, double x, double y, T dest) {
		dest.x = Math.fma(kx, x, this.x);
		dest.y = Math.fma(ky, y, this.y);
		return dest;
	}
}
