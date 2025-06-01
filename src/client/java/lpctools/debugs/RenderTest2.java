package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.lpcfymasaapi.gl.Buffer;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.VertexArray;
import lpctools.lpcfymasaapi.gl.furtherWarpped.VertexTypes;
import lpctools.shader.RenderBuffers;
import lpctools.shader.ShaderPrograms;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static lpctools.util.MathUtils.inverseOffsetMatrix4f;

public class RenderTest2 {
    private static final ByteBuffer indexByteBuffer = MemoryUtil.memAlloc(16)
        .putShort((short) 0).putShort((short) 1)
        .putShort((short) 1).putShort((short) 2)
        .putShort((short) 2).putShort((short) 3)
        .putShort((short) 3).putShort((short) 0)
        .flip();
    private static final ByteBuffer vertexByteBuffer = MemoryUtil.memAlloc(64)
        .putFloat(1).putFloat(1).putFloat(-2).putInt(0x7fffffff)
        .putFloat(-1).putFloat(1).putFloat(-2).putInt(0x7fffffff)
        .putFloat(-1).putFloat(-1).putFloat(-2).putInt(0x7fffffff)
        .putFloat(1).putFloat(-1).putFloat(-2).putInt(0x7fffffff)
        .flip();
    private static boolean initialized = false;
    private static final Buffer vertexBuffer = new Buffer();
    private static final Buffer indexBuffer = new Buffer();
    private static final VertexArray vertexArray = new VertexArray();
    private static final RenderBuffers.SimpleRenderBuffer buf
        = RenderBuffers.simpleRenderBuffer(Constants.BufferMode.STATIC_DRAW);
    static {
        buf.putFloat(1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putFloat(1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putIndex(0).putIndex(1).putIndex(1).putIndex(2)
            .putIndex(2).putIndex(3).putIndex(3).putIndex(0);
    }
    private static void init(){
        vertexArray.bind();
        indexBuffer.data(indexByteBuffer, Constants.BufferMode.STATIC_DRAW);
        vertexBuffer.data(vertexByteBuffer, Constants.BufferMode.STATIC_DRAW);
        indexBuffer.bindAsElementArray();
        vertexBuffer.bindAsArray();
        VertexTypes.POSITION_COLOR.attribAndEnable();
        vertexArray.unbind();
        initialized = true;
    }
    public static void render(WorldRenderContext context){
        ShaderPrograms.SimpleProgram program = ShaderPrograms.SIMPLE_PROGRAM;
        GL30.glGetError();
        if(!initialized) init();
        if(!initialized) return;
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        stack.mul(matrix);
        vertexArray.bind();
        VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.DEBUG_LINES;
        program.setModelMatrix(RenderSystem.getModelViewStack());
        program.setProjectionMatrix(RenderSystem.getProjectionMatrix());
        program.useAndUniform();
        GL30.glDrawElements(drawMode.glMode, 8, GlConst.GL_UNSIGNED_SHORT, 0);
        vertexArray.unbind();
        buf.program.setProjectionMatrix(RenderSystem.getProjectionMatrix());
        buf.program.setModelMatrix(RenderSystem.getModelViewStack());
        buf.renderWithIndexes(Constants.DrawMode.LINES);
        stack.popMatrix();
    }
}
