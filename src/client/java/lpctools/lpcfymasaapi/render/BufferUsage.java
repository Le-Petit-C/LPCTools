package lpctools.lpcfymasaapi.render;

import static org.lwjgl.opengl.GL15.*;

public enum BufferUsage {
	STREAM_WRITE(GL_STREAM_DRAW ),
	STREAM_READ (GL_STREAM_READ ),
	STREAM_COPY (GL_STREAM_COPY ),
	STATIC_WRITE(GL_STATIC_DRAW ),
	STATIC_READ (GL_STATIC_READ ),
	STATIC_COPY (GL_STATIC_COPY ),
	DYNAMIC_WRITE(GL_DYNAMIC_DRAW),
	DYNAMIC_READ(GL_DYNAMIC_READ),
	DYNAMIC_COPY(GL_DYNAMIC_COPY);
	public final int glValue;
	BufferUsage(int glValue) { this.glValue = glValue; }
}
