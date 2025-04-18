package lpctools.debugs;

import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
//import org.joml.Vector3f;

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
        MatrixStack matrixStack = context.matrixStack();
        Matrix4f transformationMatrix;
        if(matrixStack != null) transformationMatrix = matrixStack.peek().getPositionMatrix();
        else transformationMatrix = new Matrix4f();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        //Vector3f cam = context.camera().getPos().toVector3f();
        //float x = cam.x, y = cam.y, z = cam.z;
        float theta = Clock.systemUTC().millis() % 6283 / 1000.0f;
        float alpha = MathHelper.PI * 2 / 3;
        buffer.vertex(transformationMatrix, MathHelper.cos(theta), 0, MathHelper.sin(theta)).color(0xFFFF0000);
        buffer.vertex(transformationMatrix, MathHelper.cos(theta + alpha), 0, MathHelper.sin(theta + alpha)).color(0xFF00FF00);
        buffer.vertex(transformationMatrix, MathHelper.cos(theta - alpha), 0, MathHelper.sin(theta - alpha)).color(0xFF0000FF);
        try(ShaderProgram ignored = RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)){
            RenderSystem.disableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.draw(buffer.end());
            //BufferRenderer.drawWithGlobalProgram(buffer.end());
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
