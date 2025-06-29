package lpctools.debugs;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.gl.*;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.shader.FragmentShaders;
import lpctools.shader.ShaderPrograms;
import lpctools.shader.VertexShaders;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.time.Clock;

import static lpctools.lpcfymasaapi.gl.furtherWarpped.VertexAttribElements.*;

public class InstancedRenderTest extends BooleanConfig{
    public InstancedRenderTest(@NotNull ILPCConfigList parent) {
        super(parent, "instancedRenderTest", false);
    }
    @Override public void onValueChanged() {
        super.onValueChanged();
        if(getAsBoolean()){
            if(renderInstance == null)
                renderInstance = new RenderInstance();
        }
        else if(renderInstance != null){
            renderInstance.close();
            renderInstance = null;
        }
    }
    public static final VertexAttrib POSITION_ANGLE_COLOR = new VertexAttrib(VEC3F, VEC1F, ARGB32);
    private static final Shader testInstanceDrawVertex = VertexShaders.newLPCVert("test_instance_draw.glsl");
    private static final TestInstanceDrawProgram program = new TestInstanceDrawProgram();
    private static class TestInstanceDrawProgram extends Program implements ShaderPrograms.WithFinalMatrix {
        Uniform.UniformMatrix4f matrix = addUniform(new Uniform.UniformMatrix4f(this, "matrix"));
        Uniform.Uniform1f timeAngle = addUniform(new Uniform.Uniform1f(this, "timeAngle"));
        public TestInstanceDrawProgram() {super(testInstanceDrawVertex, FragmentShaders.flat_vertex_color, POSITION_ANGLE_COLOR);}
        @Override public void setFinalMatrix(Matrix4f matrix) {this.matrix.set(matrix);}
        public void setTimeAngle(float timeAngle){this.timeAngle.setValue(timeAngle);}
    }
    private @Nullable RenderInstance renderInstance;
    private static class RenderInstance implements AutoCloseable, WorldRenderEvents.Last{
        public static final int triangleCount = 100;
        public final VertexArray vertexArray = new VertexArray();
        public final Buffer triangleInstanceBuffer = new Buffer();
        private boolean initialized = false;
        public @Override void close(){
            Registries.WORLD_RENDER_LAST.unregister(this);
            vertexArray.close();
            triangleInstanceBuffer.close();
        }
        public RenderInstance(){Registries.WORLD_RENDER_LAST.register(this);}
        private void init(MaskLayer layer){
            if(initialized) return;
            layer.bindArray(vertexArray);
            ByteBuffer buffer = MemoryUtil.memAlloc(triangleCount * 20);
            Random random = Random.create();
            for(int a = 0; a < triangleCount; ++a){
                buffer.putFloat(random.nextFloat() * 10)
                    .putFloat(random.nextFloat() * 10)
                    .putFloat(random.nextFloat() * 10)
                    .putFloat((random.nextFloat() * 2 - 1) * 3.141593f)
                    .putInt(random.nextInt() | 0xff000000);
            }
            buffer.flip();
            triangleInstanceBuffer.data(buffer, Constants.BufferMode.STATIC_DRAW);
            triangleInstanceBuffer.bindAsArray();
            program.attrib.attribAndEnable();
            GL45.glVertexAttribDivisor(0, 1);
            GL45.glVertexAttribDivisor(1, 1);
            GL45.glVertexAttribDivisor(2, 1);
            MemoryUtil.memFree(buffer);
            initialized = true;
        }
        @Override public void onLast(WorldRenderContext context) {
            try(MaskLayer layer = new MaskLayer()){
                init(layer);
                layer.disableCullFace().enableDepthTest().disableBlend();
                layer.bindArray(vertexArray);
                Matrix4f matrix = MathUtils.inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
                context.positionMatrix().mul(matrix, matrix);
                context.projectionMatrix().mul(matrix, matrix);
                program.setFinalMatrix(matrix);
                program.setTimeAngle((Clock.systemUTC().millis() % 6283 - 3141) / 1000.0f);
                program.useAndUniform();
                Constants.DrawMode.TRIANGLES.drawArraysInstanced(0, 3, triangleCount);
            }
        }
    }
}
