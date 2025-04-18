package lpctools.debugs;

import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.time.Clock;

import static lpctools.util.MathUtils.*;

public class DebugConfigs {
    public static LPCConfigList debugs;
    public static BooleanHotkeyConfig renderDebugShapes;
    public static void init(@NotNull LPCConfigPage page){
        debugs = page.addList("debugs");
        renderDebugShapes = debugs.addBooleanHotkeyConfig(
                "renderDebugShapes", false, null, DebugConfigs::renderDebugShapesValueRefreshCallback);
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
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
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
}
