package lpctools.debugs;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static lpctools.generic.GenericUtils.mayMobSpawnOn;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

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
    private static void rendDebugShapes(WorldRenderContext context) {
        RenderContext ctx = new RenderContext(RenderPipelines.DEBUG_TRIANGLE_FAN, BufferUsage.STATIC_WRITE);
        BufferBuilder buffer = ctx.getBuilder();
        Vector3f cam = context.camera().getPos().toVector3f();
        float x = cam.x, y = cam.y, z = cam.z;
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        buffer.vertex(MathHelper.cos(theta) - x, -y, MathHelper.sin(theta) - z).color(0xFFFF0000);
        buffer.vertex(MathHelper.cos(theta + alpha) - x, -y, MathHelper.sin(theta + alpha) - z).color(0xFF00FF00);
        buffer.vertex(MathHelper.cos(theta - alpha) - x, -y, MathHelper.sin(theta - alpha) - z).color(0xFF0000FF);
        try {
            BuiltBuffer meshData = buffer.endNullable();
            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }
            ctx.close();
        } catch (Exception err) {
            LPCTools.LOGGER.error("renderBlockOutline(): Draw Exception; {}", err.getMessage());
        }
        
        if(!bufferUpdated){
            ByteBuffer buffer2 = MemoryUtil.memAlloc(16 * 4);
            buffer2.putFloat(1).putFloat(1).putFloat(1).putInt(0x7fffffff);
            buffer2.putFloat(-1).putFloat(1).putFloat(1).putInt(0x7fffffff);
            buffer2.putFloat(-1).putFloat(-1).putFloat(1).putInt(0x7fffffff);
            //buffer2.putFloat(1).putFloat(1).putFloat(1).putInt(0x7fffffff);
            //buffer2.putFloat(-1).putFloat(-1).putFloat(1).putInt(0x7fffffff);
            buffer2.putFloat(1).putFloat(-1).putFloat(1).putInt(0x7fffffff);
            buffer2.flip();
            testBuffer = RenderSystem.getDevice()
                .createBuffer(null, BufferType.VERTICES, BufferUsage.STATIC_WRITE, buffer2);
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
            buffer1.putShort((short) 1);
            buffer1.putShort((short) 1);
            buffer1.putShort((short) 2);
            buffer1.putShort((short) 2);
            buffer1.putShort((short) 3);
            buffer1.putShort((short) 3);
            buffer1.putShort((short) 0);
            buffer1.flip();
            testIndexBuffer = RenderSystem.getDevice()
                .createBuffer(null, BufferType.INDICES, BufferUsage.STATIC_WRITE, buffer1);
            MemoryUtil.memFree(buffer1);
            bufferUpdated = true;
        }
        
        {
            RenderPipeline renderPipeline = MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT;
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            stack.mul(MathUtils.inverseOffsetMatrix4f(cam));
            GpuTexture gpuTexture;
            GpuTexture gpuTexture2;
            gpuTexture = framebuffer.getColorAttachment();
            gpuTexture2 = framebuffer.getDepthAttachment();
            try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(gpuTexture, OptionalInt.empty(), gpuTexture2, OptionalDouble.empty())) {
                renderPass.setPipeline(renderPipeline);
                renderPass.setVertexBuffer(0, testBuffer);
                renderPass.setIndexBuffer(testIndexBuffer, VertexFormat.IndexType.SHORT);
                renderPass.drawIndexed(0, 8);
                //renderPass.draw(0, 6);
            }
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
