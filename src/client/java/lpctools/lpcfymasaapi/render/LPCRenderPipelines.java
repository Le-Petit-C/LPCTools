package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class LPCRenderPipelines {
	public static final RenderPipeline spherePipeline =
		RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
			.withVertexFormat(VertexFormats.POSITION_COLOR_LINE_WIDTH, VertexFormat.DrawMode.TRIANGLES)
			.withVertexShader(Identifier.of("lpctools", "core/sphere"))
			.withFragmentShader(Identifier.of("lpctools", "core/sphere"))
			.withBlend(BlendFunction.TRANSLUCENT)
			.withLocation(Identifier.of("lpctools", "pipeline/sphere")).build());
}
