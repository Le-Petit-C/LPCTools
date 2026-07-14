package lpctools.debugs;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.GLStates;
import lpctools.tools.ToolUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * CPU 预变换顶点：在 CPU 上完成 ModelView 变换，
 * GPU 只做投影（bindDefaultUniforms 提供 ProjMat）。
 */
public class DebugShapes implements ToolUtils.ToolRunner, WorldRenderEvents.AfterEntities, QuietAutoCloseable {

	@Override public void registerAll(boolean b) { Registries.AFTER_ENTITIES.register(this, b); }

	GpuBuffer triangleVertexBuffer;
	GpuBuffer triangleIndexBuffer;

	private int vertexArrayId;

	private void initializeBuffers() {
		if(triangleVertexBuffer == null) {
			int stride = DefaultVertexFormat.POSITION_COLOR.getVertexSize();
			ByteBuffer buf = MemoryUtil.memAlloc(stride * 3);
			buf.putFloat( 1.000000f).putFloat(0).putFloat( 0.000000f).putInt(0xff0000ff);
			buf.putFloat(-0.500000f).putFloat(0).putFloat( 0.866025f).putInt(0xff00ff00);
			buf.putFloat(-0.500000f).putFloat(0).putFloat( -0.866025f).putInt(0xffff0000);
			buf.flip();
			try(GLStates _ = new GLStates().vertexArray(0).vertexBuffer(0)) {
				triangleVertexBuffer = new GpuBuffer(BufferType.VERTICES, BufferUsage.STATIC_WRITE, buf.remaining());
				triangleVertexBuffer.write(buf, 0);
			}
			MemoryUtil.memFree(buf);
		}
		if(vertexArrayId == 0)
			vertexArrayId = GlStateManager._glGenVertexArrays();
		if(triangleIndexBuffer == null) {
			ByteBuffer buf = MemoryUtil.memAlloc(6);
			buf.asShortBuffer().put(new short[]{0, 1, 2}).flip();
			try(GLStates _ = new GLStates().vertexArray(vertexArrayId).vertexBuffer(0)) {
				triangleIndexBuffer = new GpuBuffer(BufferType.INDICES, BufferUsage.STATIC_WRITE, 6);
				triangleIndexBuffer.write(buf, 0);
			}
			MemoryUtil.memFree(buf);
		}
	}

	@Override public void close() {
		if(triangleVertexBuffer != null)
			triangleVertexBuffer = QuietAutoCloseable.close(triangleVertexBuffer);
		if(triangleIndexBuffer != null)
			triangleIndexBuffer = QuietAutoCloseable.close(triangleIndexBuffer);
		if(vertexArrayId != 0) {
			GlStateManager._glDeleteVertexArrays(vertexArrayId);
			vertexArrayId = 0;
		}
	}

	@Override public void afterEntities(WorldRenderContext context) {
		initializeBuffers();

		var camPos = context.gameRenderer().getMainCamera().getPosition();

		//绘制三角形
		float angle = (float)(System.currentTimeMillis() / 1000.0 % (2 * Math.PI));

		Matrix4f modelView = RenderSystem.getModelViewMatrix();
		Matrix4f oldModelView = new Matrix4f(modelView);
		modelView.translate((float)-camPos.x, (float)-camPos.y, (float)-camPos.z);
		modelView.rotate(angle, 0, 1, 0);

		CompiledShaderProgram shaderProgram = RenderSystem.setShader(CoreShaders.POSITION_COLOR);
		if (shaderProgram != null) {
			shaderProgram.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, modelView,
				RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
			shaderProgram.apply();
			try(GLStates _ = new GLStates().vertexArray(vertexArrayId)
				.depthFuncEx(GlConst.GL_LEQUAL)
				.cullFace(false)
				.depthWriteMask(true)
			) {
				triangleVertexBuffer.bind();
				DefaultVertexFormat.POSITION_COLOR.setupBufferState();
				triangleIndexBuffer.bind();
				RenderSystem.drawElements(VertexFormat.Mode.TRIANGLES.asGLMode, 3, VertexFormat.IndexType.SHORT.asGLType);
			}
			shaderProgram.clear();
		}
		modelView.set(oldModelView);
	}
}
