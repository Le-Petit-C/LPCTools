package lpctools.lpcfymasaapi.render;

import java.nio.ByteBuffer;

public interface IVertex {
	void putBytes(ByteBuffer buffer);
	int putBytes(int index, ByteBuffer buffer);
}
