package lpctools.debugs;

import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.shader.RenderBuffers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import static lpctools.util.MathUtils.inverseOffsetMatrix4f;

public class RenderTest2 {
    private static final RenderBuffers.SimpleRenderBuffer buf
        = RenderBuffers.simpleRenderBuffer(Constants.BufferMode.STATIC_DRAW);
    static {
        buf.putFloat(1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putFloat(1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putIndex(0).putIndex(1).putIndex(1).putIndex(2)
            .putIndex(2).putIndex(3).putIndex(3).putIndex(0);
    }
    public static void render(WorldRenderContext context){
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        Matrix4f matrix = inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        stack.mul(matrix);
        buf.setModelMatrix(stack);
        buf.setProjectionMatrix(RenderSystem.getProjectionMatrix());
        buf.renderWithIndexes(Constants.DrawMode.LINES);
        stack.popMatrix();
    }
}
