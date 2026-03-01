package lpctools.lpcfymasaapi.render;

import java.nio.ByteBuffer;

public interface ILineWidthVertex extends IVertex {
	void setLineWidth(float lineWidth);
	float getLineWidth();
	
	@Override default void putBytes(ByteBuffer buffer){ buffer.putFloat(getLineWidth()); }
	@Override default int putBytes(int index, ByteBuffer buffer){
		buffer.putFloat(index, getLineWidth());
		return index + 4;
	}
}
