package lpctools.tools.singletool;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.time.Clock;

public class SingleTool {
    public static BooleanConfig slightXRay;
    public static void init(ThirdListConfig STConfig){
        slightXRay = STConfig.addBooleanConfig("slightXRay", false, new SlightXRay());
        WorldRenderEvents.LAST.register(context -> {
            //if(!testRendererConfig.getAsBoolean()) return;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            float theta = Clock.systemDefaultZone().millis() % 6283 / 1000.0f;
            float alpha = 2 * MathHelper.PI / 3;
            Vector3f pos = context.camera().getPos().toVector3f();
            Matrix4f matrix4f = new Matrix4f();
            Vector4f vector4f = new Vector4f();
            vector4f.set(-pos.x, -pos.y, -pos.z, 1);
            matrix4f.setColumn(3, vector4f);
            buffer.vertex(matrix4f, MathHelper.cos(theta), 0, MathHelper.sin(theta)).color(0xFFFF0000);
            buffer.vertex(matrix4f, MathHelper.cos(theta + alpha), 0, MathHelper.sin(theta + alpha)).color(0xFF00FF00);
            buffer.vertex(matrix4f, MathHelper.cos(theta - alpha), 0, MathHelper.sin(theta - alpha)).color(0xFF0000FF);


            // Make sure the correct shader for your chosen vertex format is set!
            // You can find all the shaders in the ShaderProgramKeys class.
            //RenderSystem.disableCull();
            GlStateManager._disableCull();
            //RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw the buffer onto the screen.

            //BufferRenderer.drawWithGlobalProgram(buffer.end());
        });
    }
}
