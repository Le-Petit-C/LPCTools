package lpctools.lpcfymasaapi.render.translucentShapes;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.jetbrains.annotations.NotNull;

public record RenderOption(RenderPipeline pipeline, boolean useDepthBuffer, boolean doBias, TranslateMethod translateMethod,
						   RenderTiming timing, @NotNull ImmutableSet<ExtraBindings> extraBindings) {
	// 具有相同参数的RenderOption应当在HashMap中对应同一个RenderInstance
	// Java中Record类对hashCode和equals默认的重载能够满足这个要求，故不再手动重载
}
