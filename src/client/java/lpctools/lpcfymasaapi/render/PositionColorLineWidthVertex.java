package lpctools.lpcfymasaapi.render;

import org.joml.Vector3d;

public class PositionColorLineWidthVertex implements IPositionColorLineWidthVertex {
	double x, y, z;
	int color;
	float lineWidth;
	public PositionColorLineWidthVertex(double x, double y, double z, int color, float lineWidth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
		this.lineWidth = lineWidth;
	}
	public PositionColorLineWidthVertex() { this(0, 0, 0, 0, 1); }
	@SuppressWarnings("unused") public PositionColorLineWidthVertex(Vector3d position, int color, float lineWidth) {
		this(position.x, position.y, position.z, color, lineWidth);
	}
	
	@Override public void setColor(int color) { this.color = color; }
	@Override public int getColor() { return color; }
	@Override public double getX() { return x; }
	@Override public double getY() { return y; }
	@Override public double getZ() { return z; }
	@Override public void setX(double x) { this.x = x; }
	@Override public void setY(double y) { this.y = y; }
	@Override public void setZ(double z) { this.z = z; }
	@Override public void setLineWidth(float lineWidth) { this.lineWidth = lineWidth; }
	@Override public float getLineWidth() { return lineWidth; }
	
	@Override public boolean equals(Object obj) {
		if(!(obj instanceof PositionColorLineWidthVertex o)) return false;
		return x == o.x && y == o.y && z == o.z && color == o.color && lineWidth == o.lineWidth;
	}
	
	@Override public int hashCode() {
		int res = Double.hashCode(x);
		res = res * 31 + Double.hashCode(y);
		res = res * 31 + Double.hashCode(z);
		res = res * 31 + Integer.hashCode(color);
		res = res * 31 + Float.hashCode(lineWidth);
		return res;
	}
}
