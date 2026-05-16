package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class LPCRenderPipelines {
	public static final RenderPipeline spherePipeline =
		RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH, VertexFormat.Mode.TRIANGLES)
			.withVertexShader(Identifier.fromNamespaceAndPath("lpctools", "core/sphere"))
			.withFragmentShader(Identifier.fromNamespaceAndPath("lpctools", "core/sphere"))
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withLocation(Identifier.fromNamespaceAndPath("lpctools", "pipeline/sphere")).build());
}
