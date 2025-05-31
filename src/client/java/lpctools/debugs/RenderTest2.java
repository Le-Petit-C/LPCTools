package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static lpctools.util.DataUtils.notifyPlayer;
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
    private static int indexBuffer;
    private static int vertexBuffer;
    private static int vertexArrayId;
    private static int program;
    private static int modelViewMatrix;
    private static int projectionMatrix;
    private static void init(){
        program = GL30.glCreateProgram();
        GL30.glAttachShader(program, RenderTest.simple_translation_vertex_shader.getGlShaderId());
        GL30.glAttachShader(program, RenderTest.no_change_frag_shader.getGlShaderId());
        GL30.glLinkProgram(program);
        modelViewMatrix = GL30.glGetUniformLocation(program, "modelViewMatrix");
        projectionMatrix = GL30.glGetUniformLocation(program, "projectionMatrix");
        notifyPlayer(String.format("modelViewMatrixPosition:%d", modelViewMatrix), false);
        notifyPlayer(String.format("projectionMatrixPosition:%d", projectionMatrix), false);
        GL30.glUseProgram(program);
        indexBuffer = GL30.glGenBuffers();
        vertexBuffer = GL30.glGenBuffers();
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
        //GL30.glEnable(GL30.GL_BLEND);
        GL30.glBindVertexArray(0);
        initialized = true;
    }
    private static int frameCount = 0;
    public static void render(WorldRenderContext context){
        GL30.glGetError();
        if(!initialized) init();
        if(!initialized) return;
        GL30.glBindVertexArray(vertexArrayId);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.DEBUG_LINES;
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(matrix);
        GL30.glUseProgram(program);
        GL30.glUniformMatrix4fv(modelViewMatrix, false, RenderSystem.getModelViewStack().get(new float[16]));
        GL30.glUniformMatrix4fv(projectionMatrix, false, RenderSystem.getProjectionMatrix().get(new float[16]));
        GL30.glDrawElements(drawMode.glMode, 8, GlConst.GL_UNSIGNED_SHORT, 0);
        stack.popMatrix();
        GL30.glBindVertexArray(0);
        ++frameCount;
    }
    private static void putGlError(String pos){
        if(frameCount % 1024 != 0 && !pos.startsWith("a")) return;
        int err = GL30.glGetError();
        String info = ofGLError(err, null);
        if(info == null) return;
        notifyPlayer(Text.of(String.format("%s:%x:%s", pos, err, info)), false);
    }
    private static String ofGLError(int glError, String def){
        return switch (glError) {
            case GL30.GL_INVALID_ENUM -> "GL_INVALID_ENUM";
            case GL30.GL_INVALID_VALUE -> "GL_INVALID_VALUE";
            case GL30.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
            case GL30.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
            case GL30.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
            case GL30.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
            default -> def;
        };
    }
}
