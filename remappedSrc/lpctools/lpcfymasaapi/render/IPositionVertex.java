package lpctools.lpcfymasaapi.render;

import org.joml.Vector3d;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface IPositionVertex extends IVertex {
	double getX();
	double getY();
	double getZ();
	void setX(double x);
	void setY(double y);
	void setZ(double z);
	default Vector3d getPosition(Vector3d res) { return res.set(getX(), getY(), getZ()); }
	default Vector3d getPosition() { return new Vector3d(getX(), getY(), getZ()); }
	default void setPosition(double x, double y, double z) { setX(x); setY(y); setZ(z); }
	default void setPosition(Vector3d position) {setX(position.x); setY(position.y); setZ(position.z); }
	
	default void putBytesRelatively(ByteBuffer buffer, double camX, double camY, double camZ){
		buffer
			.putFloat((float)(getX() - camX))
			.putFloat((float)(getY() - camY))
			.putFloat((float)(getZ() - camZ));
	}
	
	default void putBytesRelatively(ByteBuffer buffer, Vector3d cameraPos){
		putBytesRelatively(buffer, cameraPos.x, cameraPos.y, cameraPos.z);
	}
	
	default int putBytesRelatively(int index, ByteBuffer buffer, double camX, double camY, double camZ){
		buffer.putFloat(index, (float)(getX() - camX))
			.putFloat(index + 4, (float)(getY() - camY))
			.putFloat(index + 8, (float)(getZ() - camZ));
		return index + 12;
	}
	
	default int putBytesRelatively(int index, ByteBuffer buffer, Vector3d cameraPos){
		return putBytesRelatively(index, buffer, cameraPos.x, cameraPos.y, cameraPos.z);
	}
	
	@Override default void putBytes(ByteBuffer buffer){
		putBytesRelatively(buffer, new Vector3d());
	}
	@Override default int putBytes(int index, ByteBuffer buffer){
		return putBytesRelatively(index, buffer, new Vector3d());
	}
}
