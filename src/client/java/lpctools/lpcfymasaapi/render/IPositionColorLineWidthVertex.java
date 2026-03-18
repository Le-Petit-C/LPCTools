package lpctools.lpcfymasaapi.render;

import org.joml.Vector3d;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface IPositionColorLineWidthVertex extends IPositionVertex, IColorVertex, ILineWidthVertex {
	default void setPositionColorLineWidth(double x, double y, double z, int color, float lineWidth) {
		setPosition(x, y, z);
		setColor(color);
		setLineWidth(lineWidth);
	}
	
	default void setPositionColorLineWidth(Vector3d position, int color, float lineWidth) {
		setPosition(position);
		setColor(color);
		setLineWidth(lineWidth);
	}
	
	default void setPositionColor(Vector3d position, int color) {
		setPosition(position);
		setColor(color);
	}
	
	default void setPositionColor(double x, double y, double z, int color) {
		setPosition(x, y, z);
		setColor(color);
	}
	
	@Override default void putBytesRelatively(ByteBuffer buffer, double camX, double camY, double camZ) {
		IPositionVertex.super.putBytesRelatively(buffer, camX, camY, camZ);
		IColorVertex.super.putBytes(buffer);
		ILineWidthVertex.super.putBytes(buffer);
	}
	@Override default int putBytesRelatively(int index, ByteBuffer buffer, double camX, double camY, double camZ) {
		return ILineWidthVertex.super.putBytes(
			IColorVertex.super.putBytes(
				IPositionVertex.super.putBytesRelatively(
					index, buffer, camX, camY, camZ
				), buffer
			), buffer
		);
	}
	
	@Override default void putBytes(ByteBuffer buffer) {
		IPositionVertex.super.putBytes(buffer);
		IColorVertex.super.putBytes(buffer);
		ILineWidthVertex.super.putBytes(buffer);
	}
	
	@Override default int putBytes(int index, ByteBuffer buffer) {
		return ILineWidthVertex.super.putBytes(
			IColorVertex.super.putBytes(
				IPositionVertex.super.putBytes(
					index, buffer
				), buffer
			), buffer
		);
	}
}
