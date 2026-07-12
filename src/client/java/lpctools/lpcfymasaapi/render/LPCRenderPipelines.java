package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static fi.dy.masa.malilib.render.MaLiLibPipelines.*;
import static net.minecraft.client.renderer.RenderPipelines.*;

public class LPCRenderPipelines {
	static {
		try {
			Class.forName(RenderPipelines.class.getName());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public static final RenderPipeline spherePipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH, VertexFormat.Mode.TRIANGLES)
		.withVertexShader(getId("core/sphere"))
		.withFragmentShader(getId("core/sphere"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withLocation(getId("pipeline/sphere")).build();

	public static RenderPipeline positionColorPipeline(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull) {
		return PositionColorPipelineLazyInitializer.positionColorPipeline(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull);
	}

	public static RenderPipeline positionColorPipeline(boolean isLine, boolean depthless) {
		return PositionColorPipelineLazyInitializer.positionColorPipeline(isLine, true, !depthless, !depthless, depthless ? OffsetMode.NONE : OffsetMode.OFFSET_1, false);
	}

	public enum OffsetMode {
		NONE { @Override RenderPipeline eigenPipeline() { return POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH; } },
		OFFSET_1 { @Override RenderPipeline eigenPipeline() { return POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_OFFSET_1; } },
		OFFSET_2 { @Override RenderPipeline eigenPipeline() { return POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_OFFSET_2; } },
		OFFSET_3 { @Override RenderPipeline eigenPipeline() { return POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_OFFSET_3; } };
		abstract RenderPipeline eigenPipeline();
		private static DepthStencilState depthStencilStateOrDefault(@Nullable DepthStencilState state) {
			if(state != null) return state;
			else return new DepthStencilState(CompareOp.ALWAYS_PASS, false);
		}
		private DepthStencilState depthStencilState() { return depthStencilStateOrDefault(eigenPipeline().getDepthStencilState()); }
		public float depthBiasScaleFactor() { return depthStencilState().depthBiasScaleFactor(); }
		public float depthBiasConstant() { return depthStencilState().depthBiasConstant(); }
		public static @Nullable OffsetMode pipelineOffsetMode(RenderPipeline pipeline) {
			DepthStencilState depthStencilState = depthStencilStateOrDefault(pipeline.getDepthStencilState());
			for(OffsetMode mode: OffsetMode.values()) {
				if(
					mode.depthBiasScaleFactor() == depthStencilState.depthBiasScaleFactor()
					&& mode.depthBiasConstant() == depthStencilState.depthBiasConstant()
				) return mode;
			}
			return null;
		}
	}

	private static class PositionColorPipelineLazyInitializer {
		private static final Int2ObjectOpenHashMap<RenderPipeline> positionColorPipelines = new Int2ObjectOpenHashMap<>();

		private static class IndexAdvancer {
			int val = 0;
			@Contract(value = "_ -> this", mutates = "this")
			IndexAdvancer advance(boolean b) { val = (val << 1) + (b ? 1 : 0); return this; }
			@Contract(value = "_ -> this", mutates = "this")
			<T extends Enum<?>> IndexAdvancer advance(T value) { val = val * value.getDeclaringClass().getEnumConstants().length + value.ordinal(); return this; }
		}

		@Contract(pure = true)
		private static int getPipelineIndex(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull) {
			return new IndexAdvancer()
				.advance(isLine)
				.advance(translucentBlend)
				.advance(depthTest)
				.advance(depthWrite)
				.advance(offsetMode)
				.advance(cull)
				.val;
		}

		private static void setPipeline(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull, RenderPipeline pipeline) {
			positionColorPipelines.put(getPipelineIndex(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull), pipeline);
		}

		private static void setPipeline(RenderPipeline pipeline) {
			boolean isLine;
			switch(pipeline.getVertexFormatMode()) {
				case TRIANGLES, QUADS -> isLine = false;
				case DEBUG_LINES -> isLine = true;
				default -> {return;}
			}
			OffsetMode offsetMode = OffsetMode.pipelineOffsetMode(pipeline);
			if(offsetMode == null) return;
			boolean translucentBlend = (pipeline.getColorTargetState() instanceof ColorTargetState state ? state.blendFunction().orElse(null) : null) == BlendFunction.TRANSLUCENT;
			DepthStencilState depth = Objects.requireNonNullElseGet(pipeline.getDepthStencilState(), ()->new DepthStencilState(CompareOp.ALWAYS_PASS, false));
			boolean depthTest;
			if(depth.depthTest() == CompareOp.ALWAYS_PASS) depthTest = false;
			else if(depth.depthTest() == CompareOp.GREATER_THAN_OR_EQUAL) depthTest = true;
			else return;
			boolean depthWrite = depth.writeDepth();
			boolean cull = pipeline.isCull();
			setPipeline(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull, pipeline);
		}

		private static void setPipelines(RenderPipeline... pipelines) {
			for(RenderPipeline pipeline : pipelines)
				setPipeline(pipeline);
		}

		static {
			setPipelines(
				DEBUG_QUADS,
				DEBUG_FILLED_BOX,
				LINES,
				LINES_TRANSLUCENT,
				POSITION_COLOR_TRANSLUCENT_NO_DEPTH_NO_CULL,
				POSITION_COLOR_TRANSLUCENT_NO_DEPTH,
				POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_NO_CULL,
				POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH,
				POSITION_COLOR_TRANSLUCENT_DEPTH_MASK,
				POSITION_COLOR_TRANSLUCENT,
				POSITION_COLOR_MASA_NO_DEPTH_NO_CULL,
				POSITION_COLOR_MASA_NO_DEPTH,
				POSITION_COLOR_MASA_LEQUAL_DEPTH_NO_CULL,
				POSITION_COLOR_MASA_LEQUAL_DEPTH,
				POSITION_COLOR_MASA_DEPTH_MASK,
				POSITION_COLOR_MASA,
				DEBUG_LINES_TRANSLUCENT_NO_DEPTH_NO_CULL,
				DEBUG_LINES_TRANSLUCENT_NO_DEPTH,
				DEBUG_LINES_TRANSLUCENT_NO_CULL,
				DEBUG_LINES_TRANSLUCENT_LEQUAL_DEPTH,
				DEBUG_LINES_TRANSLUCENT,
				DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL,
				DEBUG_LINES_MASA_SIMPLE_NO_DEPTH,
				DEBUG_LINES_MASA_SIMPLE_NO_CULL,
				DEBUG_LINES_MASA_SIMPLE_LEQUAL_DEPTH,
				DEBUG_LINES_MASA_SIMPLE
			);
		}

		private static Identifier getLocationId(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull) {
			String prefix = "pipeline/position_color";
			String linePath = isLine ? "_lines" : "";
			String translucentPath = translucentBlend ? "/translucent" : "";
			String depthPath = depthTest ? (depthWrite ? "/lequal_depth_write" : "/lequal_depth") : (depthWrite ? "/depth_mask" : "/no_depth");
			String offsetPath = offsetMode == OffsetMode.NONE ? "" : "_" + offsetMode.name().toLowerCase();
			String cullPath = cull ? "" : "/no_cull";
			return getId(prefix + linePath + translucentPath + depthPath + offsetPath + cullPath);
		}

		private static RenderPipeline buildPositionColorPipeline(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull) {
			return RenderPipeline.builder(
				translucentBlend ? (isLine ? DEBUG_LINES_TRANSLUCENT_STAGE : POSITION_COLOR_TRANSLUCENT_STAGE)
					: (isLine ? DEBUG_LINES_MASA_SIMPLE_STAGE : POSITION_COLOR_MASA_STAGE))
				.withCull(cull)
				.withDepthStencilState(new DepthStencilState(depthTest ? CompareOp.LESS_THAN_OR_EQUAL : CompareOp.ALWAYS_PASS, depthWrite, offsetMode.depthBiasScaleFactor(), offsetMode.depthBiasConstant()))
				.withLocation(getLocationId(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull))
				.build();
		}

		private static RenderPipeline positionColorPipeline(boolean isLine, boolean translucentBlend, boolean depthTest, boolean depthWrite, OffsetMode offsetMode, boolean cull) {
			int index = getPipelineIndex(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull);
			return positionColorPipelines.computeIfAbsent(index, _->buildPositionColorPipeline(isLine, translucentBlend, depthTest, depthWrite, offsetMode, cull));
		}
	}

	private static Identifier getId(String id) { return Identifier.fromNamespaceAndPath("lpctools", id); }
}
