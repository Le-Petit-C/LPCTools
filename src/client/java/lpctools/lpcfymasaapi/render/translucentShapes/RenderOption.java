package lpctools.lpcfymasaapi.render.translucentShapes;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.render.GLStates;
import lpctools.lpcfymasaapi.render.OffsetMode;
import net.minecraft.client.renderer.ShaderProgram;
import org.jetbrains.annotations.NotNull;

public record RenderOption(ShaderProgram shader, VertexFormat vertexFormat, VertexFormat.Mode drawMode,
						   boolean blend, boolean cull, boolean depthTest, boolean depthWrite, OffsetMode offsetMode,
                           boolean modelOffsetOntoMatrix, RenderTiming timing, @NotNull ImmutableSet<Runnable> extraOperations) {
	// 具有相同参数的RenderOption应当在HashMap中对应同一个RenderInstance
	// Java中Record类对hashCode和equals默认的重载能够满足这个要求，故不再手动重载
	@SuppressWarnings("resource")
	public GLStates setupRenderState(int vertexArray) {
		return new GLStates()
			.vertexArray(vertexArray)
			.translucentBlend(blend)
			.depthFuncEx(depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS)
			.depthWriteMask(depthWrite)
			.cullFace(cull)
			.polygonOffsetEx(offsetMode.depthBiasScaleFactor, offsetMode.depthBiasConstant);
	}
}
