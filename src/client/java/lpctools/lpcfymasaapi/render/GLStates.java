package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.util.javaex.QuietAutoCloseable;
import org.jetbrains.annotations.Contract;
import org.lwjgl.opengl.GL30;

@SuppressWarnings("resource")
public class GLStates implements QuietAutoCloseable {
	// ---- Bit layout of `state` (single int) ----
	// bits 0-4:  old values for 5 booleans (M_* masks)
	// bits 5-9:  changed flags for 5 booleans (CH_* = M_* << 5)
	// bits 10-14: changed flags for non-boolean states
	private static final int
		M_BLEND       = 1,
		M_DEPTH_TEST  = 1 << 1,
		M_CULL_FACE   = 1 << 2,
		M_POLY_OFFSET = 1 << 3,
		M_DEPTH_WRITE = 1 << 4,
		CH_BLEND       = 1 << 5,
		CH_DEPTH_TEST  = 1 << 6,
		CH_CULL_FACE   = 1 << 7,
		CH_POLY_OFFSET = 1 << 8,
		CH_DEPTH_WRITE = 1 << 9,
		CH_POLY_OFFSET_FACTOR = 1 << 10,
		CH_POLY_OFFSET_UNITS  = 1 << 11,
		CH_DEPTH_FUNC         = 1 << 12,
		CH_BLEND_FUNC         = 1 << 13,
		CH_VAO                = 1 << 14,
		CH_VERTEX_BUFFER      = 1 << 15;

	private int state = 0;

	// ---- Old values for int/float states ----
	private float oldPolyOffsetFactor, oldPolyOffsetUnits;
	private int oldDepthFunc, oldBlendSrcRgb, oldBlendDstRgb, oldBlendSrcAlpha, oldBlendDstAlpha, oldVao, oldVertexBuffer;

	// ==== Boolean states ====

	@Contract("_->this") public GLStates blend(boolean b) {
		if ((state & CH_BLEND) == 0) {
			state |= CH_BLEND;
			if (GL30.glIsEnabled(GL30.GL_BLEND)) state |= M_BLEND;
		}
		if (b) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
		return this;
	}

	@Contract("_->this") public GLStates depthTest(boolean b) {
		if ((state & CH_DEPTH_TEST) == 0) {
			state |= CH_DEPTH_TEST;
			if (GL30.glIsEnabled(GL30.GL_DEPTH_TEST)) state |= M_DEPTH_TEST;
		}
		if (b) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
		return this;
	}

	@Contract("_->this") public GLStates cullFace(boolean b) {
		if ((state & CH_CULL_FACE) == 0) {
			state |= CH_CULL_FACE;
			if (GL30.glIsEnabled(GL30.GL_CULL_FACE)) state |= M_CULL_FACE;
		}
		if (b) RenderSystem.enableCull(); else RenderSystem.disableCull();
		return this;
	}

	@Contract("_->this") public GLStates polygonOffset(boolean b) {
		if ((state & CH_POLY_OFFSET) == 0) {
			state |= CH_POLY_OFFSET;
			if (GL30.glIsEnabled(GL30.GL_POLYGON_OFFSET_FILL)) state |= M_POLY_OFFSET;
		}
		if (b) RenderSystem.enablePolygonOffset(); else RenderSystem.disablePolygonOffset();
		return this;
	}

	@Contract("_->this")
	public GLStates depthWriteMask(boolean b) {
		if ((state & CH_DEPTH_WRITE) == 0) {
			state |= CH_DEPTH_WRITE;
			if (GlStateManager._getInteger(GL30.GL_DEPTH_WRITEMASK) != 0) state |= M_DEPTH_WRITE;
		}
		RenderSystem.depthMask(b);
		return this;
	}

	// ==== Polygon offset ====

	@Contract("_,_->this")
	public GLStates polygonOffset(float factor, float units) {
		if ((state & CH_POLY_OFFSET_FACTOR) == 0) {
			state |= CH_POLY_OFFSET_FACTOR;
			oldPolyOffsetFactor = GL30.glGetFloat(GL30.GL_POLYGON_OFFSET_FACTOR);
		}
		if ((state & CH_POLY_OFFSET_UNITS) == 0) {
			state |= CH_POLY_OFFSET_UNITS;
			oldPolyOffsetUnits = GL30.glGetFloat(GL30.GL_POLYGON_OFFSET_UNITS);
		}
		RenderSystem.polygonOffset(factor, units);
		return this;
	}

	@Contract("_,_->this")
	public GLStates polygonOffsetEx(float factor, float units) {
		return polygonOffset(factor, units).polygonOffset(factor != 0 || units != 0);
	}

	// ==== Depth func ====

	@Contract("_->this")
	public GLStates depthFunc(int func) {
		if ((state & CH_DEPTH_FUNC) == 0) {
			state |= CH_DEPTH_FUNC;
			oldDepthFunc = GlStateManager._getInteger(GL30.GL_DEPTH_FUNC);
		}
		RenderSystem.depthFunc(func);
		return this;
	}

	@Contract("_->this")
	public GLStates depthFuncEx(int depthFunc) {
		if (depthFunc != GL30.GL_ALWAYS) return depthTest(true).depthFunc(depthFunc);
		else return depthTest(false);
	}

	// ==== Blend func ====

	@Contract("_,_->this")
	public GLStates blendFunc(int sfactor, int dfactor) {
		return blendFuncSeparate(sfactor, dfactor, sfactor, dfactor);
	}

	@Contract("_,_,_,_->this")
	public GLStates blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		if ((state & CH_BLEND_FUNC) == 0) {
			state |= CH_BLEND_FUNC;
			oldBlendSrcRgb = GlStateManager._getInteger(GL30.GL_BLEND_SRC_RGB);
			oldBlendDstRgb = GlStateManager._getInteger(GL30.GL_BLEND_DST_RGB);
			oldBlendSrcAlpha = GlStateManager._getInteger(GL30.GL_BLEND_SRC_ALPHA);
			oldBlendDstAlpha = GlStateManager._getInteger(GL30.GL_BLEND_DST_ALPHA);
		}
		RenderSystem.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
		return this;
	}

	@Contract("_->this")
	public GLStates translucentBlend(boolean b) {
		if (b) return blend(true).blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		else return blend(false);
	}

	// ==== VAO & VBO ====

	@Contract("_->this")
	public GLStates vertexArray(int vao) {
		if ((state & CH_VAO) == 0) {
			state |= CH_VAO;
			oldVao = GlStateManager._getInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		}
		RenderSystem.glBindVertexArray(vao);
		return this;
	}

	@Contract("_->this")
	public GLStates vertexBuffer(int buffer) {
		if ((state & CH_VERTEX_BUFFER) == 0) {
			state |= CH_VERTEX_BUFFER;
			oldVertexBuffer = GlStateManager._getInteger(GL30.GL_ARRAY_BUFFER_BINDING);
		}
		RenderSystem.glBindBuffer(GL30.GL_ARRAY_BUFFER, buffer);
		return this;
	}

	// ==== Close: restore all changed states to original values ====

	@Override
	public void close() {
		int s = state;
		if (s == 0) return;

		if ((s & CH_BLEND) != 0) {
			if ((s & M_BLEND) != 0) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
		}
		if ((s & CH_DEPTH_TEST) != 0) {
			if ((s & M_DEPTH_TEST) != 0) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
		}
		if ((s & CH_CULL_FACE) != 0) {
			if ((s & M_CULL_FACE) != 0) RenderSystem.enableCull(); else RenderSystem.disableCull();
		}
		if ((s & CH_POLY_OFFSET) != 0) {
			if ((s & M_POLY_OFFSET) != 0) RenderSystem.enablePolygonOffset(); else RenderSystem.disablePolygonOffset();
		}
		if ((s & CH_DEPTH_WRITE) != 0)
			RenderSystem.depthMask((s & M_DEPTH_WRITE) != 0);
		if ((s & (CH_POLY_OFFSET_FACTOR | CH_POLY_OFFSET_UNITS)) != 0) {
			float f = (s & CH_POLY_OFFSET_FACTOR) != 0 ? oldPolyOffsetFactor : GL30.glGetFloat(GL30.GL_POLYGON_OFFSET_FACTOR);
			float u = (s & CH_POLY_OFFSET_UNITS) != 0 ? oldPolyOffsetUnits : GL30.glGetFloat(GL30.GL_POLYGON_OFFSET_UNITS);
			RenderSystem.polygonOffset(f, u);
		}
		if ((s & CH_DEPTH_FUNC) != 0) RenderSystem.depthFunc(oldDepthFunc);
		if ((s & CH_BLEND_FUNC) != 0)
			RenderSystem.blendFuncSeparate(oldBlendSrcRgb, oldBlendDstRgb, oldBlendSrcAlpha, oldBlendDstAlpha);
		if ((s & CH_VAO) != 0) RenderSystem.glBindVertexArray(oldVao);
		if ((s & CH_VERTEX_BUFFER) != 0) RenderSystem.glBindBuffer(GL30.GL_ARRAY_BUFFER, oldVertexBuffer);

		state = 0;
	}
}
