package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.time.Clock;

import static lpctools.generic.GenericUtils.mayMobSpawnOn;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.util.MathUtils.inverseOffsetMatrix4f;

public class DebugConfigs {
    public static BooleanConfig renderDebugShapes;
    public static BooleanConfig displayClickSlotArguments;
    public static HotkeyConfig keyActDebug;
    public static BooleanConfig showExecuteTime;
    public static HotkeyConfig getBlockStateHotkey;
    public static BooleanConfig briefBlockState;
    public static void init(){
        renderDebugShapes = addBooleanConfig(
                "renderDebugShapes", false, DebugConfigs::renderDebugShapesValueRefreshCallback);
        displayClickSlotArguments = addBooleanConfig("displayClickSlotArguments", false);
        keyActDebug = addHotkeyConfig("keyActDebug", "", (action, bind)->{
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) return false;
            player.setPitch(0);
            player.setYaw(0);
            return true;
        });
        showExecuteTime = addBooleanConfig("showExecuteTime", false);
        getBlockStateHotkey = addHotkeyConfig("getBlockStateHotkey", "", DebugConfigs::getBlockStateHotkeyCallback);
        briefBlockState = addBooleanConfig("briefBlockState", true);
    }
    
    private static final BufferAllocator bufferAllocator = new BufferAllocator(786432);
    private static void rendDebugShapes(WorldRenderContext context) {
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
        reserved(context);
    }
    private static final ByteBuffer indexByteBuffer = MemoryUtil.memAlloc(16)
        .putShort((short) 0).putShort((short) 1)
        .putShort((short) 1).putShort((short) 2)
        .putShort((short) 2).putShort((short) 3)
        .putShort((short) 3).putShort((short) 0)
        .flip();
    private static final ByteBuffer vertexByteBuffer = MemoryUtil.memAlloc(64)
        .putFloat(1).putFloat(1).putFloat(-1).putInt(0x7fffffff)
        .putFloat(-1).putFloat(1).putFloat(-1).putInt(0x7fffffff)
        .putFloat(-1).putFloat(-1).putFloat(-1).putInt(0x7fffffff)
        .putFloat(1).putFloat(-1).putFloat(-1).putInt(0x7fffffff)
        .flip();
    private static void reserved(WorldRenderContext context){
        //if(!bufferUpdated)//noinspection CommentedOutCode
        GpuBuffer vertexBuffer;
        GpuBuffer indexBuffer;
        indexBuffer = new GpuBuffer(GlBufferTarget.INDICES, GlUsage.STATIC_WRITE, indexByteBuffer);
        vertexBuffer = new GpuBuffer(GlBufferTarget.VERTICES, GlUsage.STATIC_WRITE, vertexByteBuffer);
        int vertexArrayId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        ShaderProgram program = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(ShaderProgramKeys.POSITION_COLOR);
        if (program != null) {
            VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.DEBUG_LINES;
            VertexFormat format = VertexFormats.POSITION_COLOR;
            format.setupState();
            vertexBuffer.bind();
            indexBuffer.bind();
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            program.initializeUniforms(
                drawMode, stack.mul(matrix), RenderSystem.getProjectionMatrix(),
                MinecraftClient.getInstance().getWindow());
            program.bind();
            GL30.glDrawElements(GlConst.GL_LINES, 8, GlConst.GL_UNSIGNED_SHORT, 0);
            program.unbind();
            stack.popMatrix();
        }
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vertexArrayId);
        vertexBuffer.close();
        indexBuffer.close();
    }
    private static final WorldRenderEvents.Last debugShapesRenderer = DebugConfigs::rendDebugShapes;
    private static void renderDebugShapesValueRefreshCallback(){
        if(renderDebugShapes.getAsBoolean())
            Registry.registerWorldRenderLastCallback(debugShapesRenderer);
        else Registry.unregisterWorldRenderLastCallback(debugShapesRenderer);
    }
    private static boolean getBlockStateHotkeyCallback(KeyAction action, IKeybind keybind){
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(world == null || player == null) return false;
        BlockPos pos = player.getBlockPos();
        BlockState state = world.getBlockState(pos);
        BlockState finalState;
        if(state.isAir()) finalState = world.getBlockState(pos.down());
        else finalState = state;
        if(briefBlockState.getAsBoolean()){
            String msg = "isOpaque:" + finalState.isOpaque() + '\n' +
                "isTransparent:" + finalState.isTransparent() + '\n' +
                "isOpaqueFullCube:" + finalState.isOpaqueFullCube() + '\n' +
                "mayMobSpawnOn:" + mayMobSpawnOn(finalState) + '\n';
            player.sendMessage(Text.of(msg), false);
        }
        else player.sendMessage(Text.of(finalState.toString()), false);
        return true;
    }
}
