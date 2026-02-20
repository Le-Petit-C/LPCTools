package lpctools.lpcfymasaapi.render.translucentShapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public record RenderOption(RenderPipeline pipeline, boolean useColorBuffer, boolean useDepthBuffer, RenderTiming timing) {
	public RenderOption {
		if (!useColorBuffer && !useDepthBuffer)
			throw new IllegalArgumentException("writeDepth and writeBuffer cannot be false for both");
	}
	// 具有相同参数的RenderOption应当在HashMap中对应同一个RenderInstance
	// Java中Record类对hashCode和equals默认的重载能够满足这个要求，故不再手动重载
}
