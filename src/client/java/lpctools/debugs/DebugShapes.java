package lpctools.debugs;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.lpcfymasaapi.render.translucentShapes.Sphere;
import lpctools.tools.ToolUtils;
import lpctools.util.RenderUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * CPU 预变换顶点：在 CPU 上完成 ModelView 变换，
 * GPU 只做投影（bindDefaultUniforms 提供 ProjMat）。
 */
public class DebugShapes implements ToolUtils.ToolRunner, LevelRenderEvents.BeforeTranslucentTerrain, QuietAutoCloseable {

	@Override public void registerAll(boolean b) { Registries.BEFORE_TRANSLUCENT_TERRAIN.register(this, b); }

	GpuBuffer triangleVertexBuffer;
	GpuBuffer sphereVertexBuffer;
	GpuBuffer sphereIndexBuffer;

	private void initializeBuffers() {
		if(triangleVertexBuffer == null) {
			int stride = DefaultVertexFormat.POSITION_COLOR.getVertexSize();
			ByteBuffer buf = MemoryUtil.memAlloc(stride * 3);
			buf.putFloat( 1.000000f).putFloat(0).putFloat( 0.000000f).putInt(0xff0000ff);
			buf.putFloat(-0.500000f).putFloat(0).putFloat( 0.866025f).putInt(0xff00ff00);
			buf.putFloat(-0.500000f).putFloat(0).putFloat( -0.866025f).putInt(0xffff0000);
			buf.flip();
			GpuDevice device = RenderSystem.getDevice();
			triangleVertexBuffer = device.createBuffer(
				() -> "DebugShapesTriangleVertexBuffer",
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, buf);
			MemoryUtil.memFree(buf);
		}
		if(sphereVertexBuffer == null) {
			int stride = DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH.getVertexSize();
			ByteBuffer buf = MemoryUtil.memAlloc(stride * 8);
			for(int i = 0; i < 8; ++i)
				buf.putFloat(0).putFloat(0).putFloat(0)
					.putInt(0xFFFFFFFF).putFloat(0.5f);
			buf.flip();
			GpuDevice device = RenderSystem.getDevice();
			sphereVertexBuffer = device.createBuffer(
				() -> "DebugShapesSphereVertexBuffer",
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, buf);
			MemoryUtil.memFree(buf);
		}
		if(sphereIndexBuffer == null) {
			int size = 0;
			for(int[] face : Sphere.baseIndices)
				size += face.length * 2;
			ByteBuffer buf = MemoryUtil.memAlloc(size);
			for(int[] face : Sphere.baseIndices)
				for(int index : face)
					buf.putShort((short) index);
			buf.flip();
			GpuDevice device = RenderSystem.getDevice();
			sphereIndexBuffer = device.createBuffer(
				() -> "DebugShapesSphereIndexBuffer",
				GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, buf);
			MemoryUtil.memFree(buf);
		}
	}

	@Override public void close() {
		if(triangleVertexBuffer != null)
			triangleVertexBuffer = QuietAutoCloseable.close(triangleVertexBuffer);
		if(sphereVertexBuffer != null)
			sphereVertexBuffer = QuietAutoCloseable.close(sphereVertexBuffer);
		if(sphereIndexBuffer != null)
			sphereIndexBuffer = QuietAutoCloseable.close(sphereIndexBuffer);
	}

	@Override public void beforeTranslucentTerrain(LevelRenderContext context) {
		initializeBuffers();

		var camPos = context.gameRenderer().getMainCamera().position();
		var fb = Minecraft.getInstance().getMainRenderTarget();
		GpuTextureView colorView = RenderUtils.colorAttachmentViewOrDef(fb);
		GpuTextureView depthView = RenderUtils.depthAttachmentViewOrDef(fb);
		var device = RenderSystem.getDevice();
		var enc = device.createCommandEncoder();

		//绘制三角形
		float angle = (float)(System.currentTimeMillis() / 1000.0 % (2 * Math.PI));

		Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
		modelView.translate((float)-camPos.x, (float)-camPos.y, (float)-camPos.z);
		modelView.rotate(angle, 0, 1, 0);

		GpuBufferSlice dyn = RenderSystem.getDynamicUniforms()
			.writeTransform(modelView, new Vector4f(1, 1, 1, 1), new Vector3f(), new Matrix4f());

		try (RenderPass rp = enc.createRenderPass(
				() -> "DebugTrianglePass", colorView,
				OptionalInt.empty(), depthView, OptionalDouble.empty())) {
			rp.setPipeline(LPCRenderPipelines.positionColorPipeline(false, false));
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("DynamicTransforms", dyn);
			rp.setVertexBuffer(0, triangleVertexBuffer);
			rp.draw(0, 3);
		}

		//绘制球
		try (RenderPass rp = enc.createRenderPass(
			() -> "DebugSpherePass", colorView,
			OptionalInt.empty(), depthView, OptionalDouble.empty())) {
			rp.setPipeline(LPCRenderPipelines.spherePipeline);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("DynamicTransforms", dyn);
			rp.setVertexBuffer(0, sphereVertexBuffer);
			rp.setIndexBuffer(sphereIndexBuffer, VertexFormat.IndexType.SHORT);
			rp.drawIndexed(0, 0, 6 * 2 * 6, 1);
		}
	}
}
