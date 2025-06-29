package lpctools.debugs;

import lpctools.lpcfymasaapi.gl.Buffer;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.MaskLayer;
import lpctools.lpcfymasaapi.gl.VertexArray;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static lpctools.shader.ShaderPrograms.*;

public class RenderTest1 {
    public static void render(WorldRenderContext context, MaskLayer layer){
        init(layer);
        Matrix4f finalMatrix = MathUtils.inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
        context.positionMatrix().mul(finalMatrix, finalMatrix);
        context.projectionMatrix().mul(finalMatrix, finalMatrix);
        float angle = (System.currentTimeMillis() % 6283) / 1000.0f;
        buffer.clear();
        for(int a = 0; a < 3; ++a){
            buffer.putFloat(MathHelper.cos(angle))
                .putFloat(0)
                .putFloat(MathHelper.sin(angle))
                .putInt(colors[a]);
            angle += dangle;
        }
        buffer.flip();
        layer.bindArray(array);
        vertexBuffer.data(buffer, Constants.BufferMode.DYNAMIC_DRAW);
        program.setFinalMatrix(finalMatrix);
        program.useAndUniform();
        Constants.DrawMode.TRIANGLES.drawArrays(0, 3);
    }
    private static final float dangle = 2.094398f;
    private static final PositionColorProgram program = POSITION_COLOR_PROGRAM;
    private static final int[] colors = {0xffff0000, 0xff00ff00, 0xff0000ff};
    private static final VertexArray array = new VertexArray();
    private static final Buffer vertexBuffer = new Buffer();
    private static final ByteBuffer buffer = MemoryUtil.memAlloc(48);
    private static boolean initialized = false;
    private static void init(MaskLayer layer){
        if(initialized) return;
        layer.bindArray(array);
        vertexBuffer.bindAsArray();
        program.attrib.attribAndEnable();
        initialized = true;
    }
}
