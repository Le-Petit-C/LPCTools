package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.stream.Collectors;

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
    private static GpuBuffer testBuffer;
    private static GpuBuffer testIndexBuffer;
    private static boolean bufferUpdated = false;
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
        BufferBuilder buffer = new BufferBuilder(bufferAllocator, drawMode, vertexFormat);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        buffer.vertex(matrix, MathHelper.cos(theta), 0, MathHelper.sin(theta)).color(0xFFFF0000);
        buffer.vertex(matrix, MathHelper.cos(theta + alpha), 0, MathHelper.sin(theta + alpha)).color(0xFF00FF00);
        buffer.vertex(matrix, MathHelper.cos(theta - alpha), 0, MathHelper.sin(theta - alpha)).color(0xFF0000FF);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        BuiltBuffer builtBuffer;
        BufferAllocator.CloseableBuffer closeableBuffer = bufferAllocator.getAllocated();
        int i = drawMode.getIndexCount(3);
        builtBuffer = new BuiltBuffer(closeableBuffer, new BuiltBuffer.DrawParameters(vertexFormat, 3, i, drawMode, indexType));
        GlUsage usage = GlUsage.STATIC_WRITE;
        GpuBuffer vertexBuffer_vertexBuffer = new GpuBuffer(GlBufferTarget.VERTICES, usage, 0);
        GpuBuffer vertexBuffer_indexBuffer;
        int vertexBuffer_vertexArrayId = GlStateManager._glGenVertexArrays();
        GlStateManager._glBindVertexArray(vertexBuffer_vertexArrayId);
        RenderSystem.assertOnRenderThread();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        ByteBuffer vertexBuffer1 = builtBuffer.getBuffer();
        vertexBuffer_vertexBuffer.bind();
        drawParameters.format().setupState();
        if (vertexBuffer1 != null) {
            vertexBuffer_vertexBuffer.resize(vertexBuffer1.remaining());
            vertexBuffer_vertexBuffer.copyFrom(vertexBuffer1, 0);
        }
        ByteBuffer buf = MemoryUtil.memAlloc(6);
        buf.putShort((short) 0).putShort((short) 1).putShort((short) 2);
        buf.flip();
        vertexBuffer_indexBuffer = new GpuBuffer(GlBufferTarget.INDICES, usage, buf);
        MemoryUtil.memFree(buf);
        builtBuffer.close();
        Matrix4f viewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        shaderProgram.initializeUniforms(drawMode, viewMatrix, projectionMatrix, MinecraftClient.getInstance().getWindow());
        shaderProgram.bind();
        RenderSystem.drawElements(drawMode.glMode, 3, indexType.glType);
        shaderProgram.unbind();
        vertexBuffer_vertexBuffer.close();
        vertexBuffer_indexBuffer.close();
        RenderSystem.glDeleteVertexArrays(vertexBuffer_vertexArrayId);
    }
    
    
    private static void reserved(){
        //if(true) return;
        Matrix4f matrix = new Matrix4f();
        if(!bufferUpdated)//noinspection CommentedOutCode
        {
            ByteBuffer buffer2 = MemoryUtil.memAlloc(16 * 4);
            buffer2.putFloat(1).putFloat(1).putFloat(-1).putInt(0x7fffffff);
            buffer2.putFloat(-1).putFloat(1).putFloat(-1).putInt(0x7fffffff);
            buffer2.putFloat(-1).putFloat(-1).putFloat(-1).putInt(0x7fffffff);
            //buffer2.putFloat(1).putFloat(1).putFloat(1).putInt(0x7fffffff);
            //buffer2.putFloat(-1).putFloat(-1).putFloat(1).putInt(0x7fffffff);
            buffer2.putFloat(1).putFloat(-1).putFloat(-1).putInt(0x7fffffff);
            buffer2.flip();
            testBuffer = new GpuBuffer(GlBufferTarget.VERTICES, GlUsage.STATIC_WRITE, buffer2);
            MemoryUtil.memFree(buffer2);
            /*testBuffer = RenderSystem.getDevice()
            .createBuffer(null, BufferType.VERTICES, BufferUsage.STATIC_WRITE, 6 * VertexFormats.POSITION_COLOR.getVertexSize());
            try (BufferAllocator bufferAllocator = new BufferAllocator(VertexFormats.POSITION_COLOR.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bufferAllocator, VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                builder.vertex(1, 1, 1).color(0x7fffffff);
                builder.vertex(-1, 1, 1).color(0x7fffffff);
                builder.vertex(-1, -1, 1).color(0x7fffffff);
                builder.vertex(1, 1, 1).color(0x7fffffff);
                builder.vertex(-1, -1, 1).color(0x7fffffff);
                builder.vertex(1, -1, 1).color(0x7fffffff);
                try (BuiltBuffer builtBuffer = builder.end()) {
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(testBuffer, builtBuffer.getBuffer(), 0);
                }
            }*/
            ByteBuffer buffer1 = MemoryUtil.memAlloc(16);
            buffer1.putShort((short) 0);
            //buffer1.putShort((short) 1);
            buffer1.putShort((short) 1);
            //buffer1.putShort((short) 2);
            buffer1.putShort((short) 2);
            //buffer1.putShort((short) 3);
            buffer1.putShort((short) 3);
            //buffer1.putShort((short) 0);
            buffer1.flip();
            testIndexBuffer = new GpuBuffer(GlBufferTarget.INDICES, GlUsage.STATIC_WRITE, buffer1);
            MemoryUtil.memFree(buffer1);
            bufferUpdated = true;
        }
        
        {
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            stack.mul(matrix);
            ShaderProgram program = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(ShaderProgramKeys.POSITION_COLOR);
            if(program != null){
                program.bind();
                //testBuffer.bind();
                GL30.glBindVertexArray(testBuffer.handle);
                testIndexBuffer.bind();
                Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
                framebuffer.beginWrite(false);
                program.initializeUniforms(
                    VertexFormat.DrawMode.TRIANGLE_FAN,
                    stack.get(new Matrix4f()),
                    RenderSystem.getProjectionMatrix(),
                    MinecraftClient.getInstance().getWindow());
                /*float[] arr = new float[16];
                if(program.modelViewMat != null) {
                    stack.get(arr);
                    program.modelViewMat.set(arr);
                }
                if(program.projectionMat != null) {
                    RenderSystem.getProjectionMatrix().set(arr);
                    program.projectionMat.set(arr);
                }**/
                RenderSystem.drawElements(VertexFormat.DrawMode.TRIANGLE_FAN.glMode, 4, GlConst.GL_UNSIGNED_SHORT);
                program.unbind();
            }
                //renderPass.draw(0, 6);
            stack.popMatrix();
        }
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
