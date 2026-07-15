package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.platform.GlConst;

public enum BufferType {
	VERTICES(GlConst.GL_ARRAY_BUFFER),
	INDICES(GlConst.GL_ELEMENT_ARRAY_BUFFER);
	public final int glValue;
	BufferType(int glValue) { this.glValue = glValue; }
}
