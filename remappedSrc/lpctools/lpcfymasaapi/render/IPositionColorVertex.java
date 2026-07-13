package lpctools.lpcfymasaapi.render;

import org.joml.Vector3d;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface IPositionColorVertex extends IPositionVertex, IColorVertex {
	default void setPositionColor(double x, double y, double z, int color) {
		setPosition(x, y, z);
		setColor(color);
	}
	
	default void setPositionColor(Vector3d position, int color) {
		setPosition(position);
		setColor(color);
	}
	
	@Override default void putBytesRelatively(ByteBuffer buffer, double camX, double camY, double camZ) {
		IPositionVertex.super.putBytesRelatively(buffer, camX, camY, camZ);
		IColorVertex.super.putBytes(buffer);
	}
	@Override default int putBytesRelatively(int index, ByteBuffer buffer, double camX, double camY, double camZ) {
		return IColorVertex.super.putBytes(IPositionVertex.super.putBytesRelatively(index, buffer, camX, camY, camZ), buffer);
	}
	
	@Override default void putBytes(ByteBuffer buffer) {
		IPositionVertex.super.putBytes(buffer);
		IColorVertex.super.putBytes(buffer);
	}
	
	@Override default int putBytes(int index, ByteBuffer buffer) {
		return IColorVertex.super.putBytes(IPositionVertex.super.putBytes(index, buffer), buffer);
	}
}
