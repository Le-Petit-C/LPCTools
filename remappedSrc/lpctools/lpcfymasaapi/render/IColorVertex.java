package lpctools.lpcfymasaapi.render;

import java.nio.ByteBuffer;

public interface IColorVertex extends IVertex {
	void setColor(int color);
	int getColor();
	
	@Override default void putBytes(ByteBuffer buffer){ buffer.putInt(getColor()); }
	@Override default int putBytes(int index, ByteBuffer buffer){
		buffer.putInt(index, getColor());
		return index + 4;
	}
}
