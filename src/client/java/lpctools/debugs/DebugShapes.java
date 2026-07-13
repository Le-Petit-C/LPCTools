package lpctools.debugs;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.tools.ToolUtils;
import lpctools.util.RenderUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * CPU 预变换顶点：在 CPU 上完成 ModelView 变换，
 * GPU 只做投影（bindDefaultUniforms 提供 ProjMat）。
 */
public class DebugShapes implements ToolUtils.ToolRunner, WorldRenderEvents.AfterEntities, QuietAutoCloseable {

	@Override public void registerAll(boolean b) { Registries.AFTER_ENTITIES.register(this, b); }

	GpuBuffer triangleVertexBuffer;

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
				BufferType.VERTICES, BufferUsage.STATIC_WRITE, buf);
			MemoryUtil.memFree(buf);
		}
	}

	@Override public void close() {
		if(triangleVertexBuffer != null)
			triangleVertexBuffer = QuietAutoCloseable.close(triangleVertexBuffer);
	}


	@Override public void afterEntities(WorldRenderContext context) {
		initializeBuffers();

		var camPos = context.gameRenderer().getMainCamera().getPosition();
		var fb = Minecraft.getInstance().getMainRenderTarget();
		GpuTexture colorView = RenderUtils.colorAttachmentViewOrDef(fb);
		GpuTexture depthView = RenderUtils.depthAttachmentViewOrDef(fb);
		var device = RenderSystem.getDevice();
		var enc = device.createCommandEncoder();

		//绘制三角形
		float angle = (float)(System.currentTimeMillis() / 1000.0 % (2 * Math.PI));

		Matrix4f modelView = RenderSystem.getModelViewMatrix();
		Matrix4f oldModelView = new Matrix4f(modelView);
		modelView.translate((float)-camPos.x, (float)-camPos.y, (float)-camPos.z);
		modelView.rotate(angle, 0, 1, 0);

		try (RenderPass rp = enc.createRenderPass(colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
			rp.setPipeline(LPCRenderPipelines.positionColorPipeline(false, false));
			rp.setVertexBuffer(0, triangleVertexBuffer);
			rp.draw(0, 3);
		}
		modelView.set(oldModelView);
	}
}
