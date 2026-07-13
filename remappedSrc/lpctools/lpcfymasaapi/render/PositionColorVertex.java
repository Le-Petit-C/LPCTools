package lpctools.lpcfymasaapi.render;

import org.joml.Vector3d;

public class PositionColorVertex implements IPositionColorVertex {
	double x, y, z;
	int color;
	public PositionColorVertex(double x, double y, double z, int color) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
	}
	public PositionColorVertex() { this(0, 0, 0, 0); }
	@SuppressWarnings("unused") public PositionColorVertex(Vector3d position, int color) {
		this(position.x, position.y, position.z, color);
	}
	
	@Override public void setColor(int color) { this.color = color; }
	@Override public int getColor() { return color; }
	@Override public double getX() { return x; }
	@Override public double getY() { return y; }
	@Override public double getZ() { return z; }
	@Override public void setX(double x) { this.x = x; }
	@Override public void setY(double y) { this.y = y; }
	@Override public void setZ(double z) { this.z = z; }
	
	@Override public boolean equals(Object obj) {
		if(!(obj instanceof PositionColorVertex o)) return false;
		return x == o.x && y == o.y && z == o.z && color == o.color;
	}
	
	@Override public int hashCode() {
		int res = Double.hashCode(x);
		res = res * 31 + Double.hashCode(y);
		res = res * 31 + Double.hashCode(z);
		res = res * 31 + Integer.hashCode(color);
		return res;
	}
}
