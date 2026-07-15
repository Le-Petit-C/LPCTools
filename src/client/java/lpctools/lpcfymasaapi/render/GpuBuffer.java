package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import lpctools.util.javaex.QuietAutoCloseable;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public class GpuBuffer implements QuietAutoCloseable {
	public final int handle;
	public final int size;
	public final BufferState state;
	public GpuBuffer(BufferState state, int size) {
		handle = GlStateManager._glGenBuffers();
		this.size = size;
		this.state = state;
		bind();
		GlStateManager._glBufferData(state.type.glValue, size, state.usage.glValue);
	}
	public GpuBuffer(BufferType type, BufferUsage usage, int size) {
		this(BufferState.fromOptions(type, usage), size);
	}
	public void write(ByteBuffer buffer, int offset) {
		bind();
		GL15.glBufferSubData(state.type.glValue, offset, buffer);
	}
	public void bind() { GlStateManager._glBindBuffer(state.type.glValue, handle); }
	@Override public void close() { GlStateManager._glDeleteBuffers(handle); }
}
