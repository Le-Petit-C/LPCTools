package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
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
    private static int vertexArrayId;
    private static void init(){
        int indexBuffer = GL30.glGenBuffers();
        int vertexBuffer = GL30.glGenBuffers();
        vertexArrayId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffer);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexByteBuffer, GL30.GL_STATIC_DRAW);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexByteBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 16, 0);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(1, 3, GL30.GL_UNSIGNED_BYTE, true, 16, 12);
        GL30.glEnableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        initialized = true;
    }
    public static void render(WorldRenderContext context){
        ShaderPrograms.SimpleProgram program = ShaderPrograms.simple_program;
        GL30.glGetError();
        if(!initialized) init();
        if(!initialized) return;
        GL30.glBindVertexArray(vertexArrayId);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.DEBUG_LINES;
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(matrix);
        GL30.glUseProgram(ShaderPrograms.simple_program.getGlProgramId());
        program.getModMatUniform().set(RenderSystem.getModelViewStack());
        program.getProjMatUniform().set(RenderSystem.getProjectionMatrix());
        program.useAndUniform();
        GL30.glDrawElements(drawMode.glMode, 8, GlConst.GL_UNSIGNED_SHORT, 0);
        stack.popMatrix();
        GL30.glBindVertexArray(0);
    }
}
