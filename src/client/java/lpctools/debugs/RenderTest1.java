package lpctools.debugs;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.time.Clock;

public class RenderTest1 {
    public static void render(WorldRenderContext context) {
        /*Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        buffer.vertex(matrix, MathHelper.cos(theta), 0, MathHelper.sin(theta)).color(0xFFFF0000);
        buffer.vertex(matrix, MathHelper.cos(theta + alpha), 0, MathHelper.sin(theta + alpha)).color(0xFF00FF00);
        buffer.vertex(matrix, MathHelper.cos(theta - alpha), 0, MathHelper.sin(theta - alpha)).color(0xFF0000FF);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(buffer.end());*/
        ShaderProgram shaderProgram = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(ShaderProgramKeys.POSITION_COLOR);
        assert shaderProgram != null;
        VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.TRIANGLES;
        VertexFormat.IndexType indexType = VertexFormat.IndexType.SHORT;
        VertexFormat vertexFormat = VertexFormats.POSITION_COLOR;
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        GlUsage usage = GlUsage.STATIC_WRITE;
        GpuBuffer vertexBuffer_indexBuffer;
        ByteBuffer vertexBuffer1 = MemoryUtil.memAlloc(48);
        ByteBuffer buf = MemoryUtil.memAlloc(6);
        Vector3f p = context.camera().getPos().toVector3f();
        vertexBuffer1.putFloat(MathHelper.cos(theta) - p.x).putFloat(-p.y).putFloat(MathHelper.sin(theta) - p.z).putInt(0xFFFF0000);
        vertexBuffer1.putFloat(MathHelper.cos(theta + alpha) - p.x).putFloat(-p.y).putFloat(MathHelper.sin(theta + alpha) - p.z).putInt(0xFF00FF00);
        vertexBuffer1.putFloat(MathHelper.cos(theta - alpha) - p.x).putFloat(-p.y).putFloat(MathHelper.sin(theta - alpha) - p.z).putInt(0xFF0000FF);
        buf.putShort((short) 0).putShort((short) 1).putShort((short) 2);
        vertexBuffer1.flip();
        buf.flip();
        GpuBuffer vertexBuffer_vertexBuffer = new GpuBuffer(GlBufferTarget.VERTICES, usage, vertexBuffer1);
        vertexBuffer_indexBuffer = new GpuBuffer(GlBufferTarget.INDICES, usage, buf);
        MemoryUtil.memFree(vertexBuffer1);
        MemoryUtil.memFree(buf);
        
        Matrix4f viewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        int vertexArrayId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        vertexFormat.setupState();
        shaderProgram.initializeUniforms(drawMode, viewMatrix, projectionMatrix, MinecraftClient.getInstance().getWindow());
        vertexBuffer_vertexBuffer.bind();
        vertexBuffer_indexBuffer.bind();
        shaderProgram.bind();
        GL30.glDrawElements(drawMode.glMode, 3, indexType.glType, 0);
        shaderProgram.unbind();
        vertexBuffer_vertexBuffer.close();
        vertexBuffer_indexBuffer.close();
        GL30.glDeleteVertexArrays(vertexArrayId);
    }
}
