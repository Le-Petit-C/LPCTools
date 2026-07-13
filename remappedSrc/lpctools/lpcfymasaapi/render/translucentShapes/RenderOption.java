package lpctools.lpcfymasaapi.render.translucentShapes;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderProgram;
import org.jetbrains.annotations.NotNull;

public record RenderOption(ShaderProgram shader, VertexFormat vertexFormat, VertexFormat.Mode drawMode, boolean useColorBuffer, boolean useDepthBuffer,
						   TranslateMethod translateMethod, RenderTiming timing, @NotNull ImmutableSet<Runnable> extraOperations) {
	public RenderOption {
		if (!useColorBuffer && !useDepthBuffer)
			throw new IllegalArgumentException("writeDepth and writeBuffer cannot be false for both");
	}
	// 具有相同参数的RenderOption应当在HashMap中对应同一个RenderInstance
	// Java中Record类对hashCode和equals默认的重载能够满足这个要求，故不再手动重载
}
