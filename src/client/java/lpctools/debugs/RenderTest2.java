package lpctools.debugs;

import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.shader.RenderBuffers;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.joml.Matrix4f;

public class RenderTest2 {
    private static final RenderBuffers.PositionColorBuffer buf
        = RenderBuffers.positionColorBuffer(Constants.BufferMode.STATIC_DRAW);
    static {
        buf.putFloat(1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(1).putFloat(2).putInt(0x7fffffff)
            .putFloat(-1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putFloat(1).putFloat(-1).putFloat(2).putInt(0x7fffffff)
            .putIndex(0).putIndex(1).putIndex(1).putIndex(2)
            .putIndex(2).putIndex(3).putIndex(3).putIndex(0);
    }
    public static void render(WorldRenderContext context){
        Matrix4f finalMatrix = MathUtils.inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        context.matrixStack().peek().getPositionMatrix().mul(finalMatrix, finalMatrix);
        context.projectionMatrix().mul(finalMatrix, finalMatrix);
        buf.setFinalMatrix(finalMatrix);
        buf.renderWithIndexes(Constants.DrawMode.LINES);
    }
}
