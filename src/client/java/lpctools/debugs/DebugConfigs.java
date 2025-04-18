package lpctools.debugs;

import com.mojang.blaze3d.buffers.BufferUsage;
import fi.dy.masa.malilib.render.RenderContext;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.time.Clock;

public class DebugConfigs {
    public static LPCConfigList debugs;
    public static BooleanHotkeyConfig renderDebugShapes;
    public static void init(@NotNull LPCConfigPage page){
        debugs = page.addList("debugs");
        renderDebugShapes = debugs.addBooleanHotkeyConfig(
                "renderDebugShapes", false, null, DebugConfigs::renderDebugShapesValueRefreshCallback);
    }
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
