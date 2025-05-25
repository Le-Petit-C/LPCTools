package lpctools.debugs;

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
//import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

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
    private static void rendDebugShapes(WorldRenderContext context) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        buffer.vertex(matrix, MathHelper.cos(theta), 0, MathHelper.sin(theta)).color(0xFFFF0000);
        buffer.vertex(matrix, MathHelper.cos(theta + alpha), 0, MathHelper.sin(theta + alpha)).color(0xFF00FF00);
        buffer.vertex(matrix, MathHelper.cos(theta - alpha), 0, MathHelper.sin(theta - alpha)).color(0xFF0000FF);
        RenderSystem.disableCull();
        //RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    private static @Nullable WorldRenderEvents.Last debugShapesRenderer;
    private static void renderDebugShapesValueRefreshCallback(){
        if(renderDebugShapes.getAsBoolean()){
            if(debugShapesRenderer == null)
                Registry.registerWorldRenderLastCallback(
                        debugShapesRenderer = DebugConfigs::rendDebugShapes
                );
        }
        else{
            if(debugShapesRenderer != null){
                Registry.unregisterWorldRenderLastCallback(debugShapesRenderer);
                debugShapesRenderer = null;
            }
        }
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
                //"isTransparent:" + finalState.isTransparent() + '\n' +
                //"isOpaqueFullCube:" + finalState.isOpaqueFullCube() + '\n' +
                "mayMobSpawnOn:" + mayMobSpawnOn(finalState) + '\n';
            player.sendMessage(Text.of(msg), false);
        }
        else player.sendMessage(Text.of(finalState.toString()), false);
        return true;
    }
}
