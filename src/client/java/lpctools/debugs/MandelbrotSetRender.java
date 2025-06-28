package lpctools.debugs;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.gl.*;
import lpctools.lpcfymasaapi.gl.furtherWarpped.ArrayListCachedRenderBuffer;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.shader.ShaderPrograms;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.lang.Math;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.lpcfymasaapi.gl.furtherWarpped.VertexAttribElements.*;
import static lpctools.shader.FragmentShaders.*;
import static lpctools.shader.VertexShaders.*;

public class MandelbrotSetRender extends ThirdListConfig implements WorldRenderEvents.DebugRender {
    public final IntegerConfig maxDepth;
    public final DoubleConfig stretch;
    public MandelbrotSetRender(ILPCConfigList parent) {
        super(parent, "mandelbrotSet", false);
        try(ConfigListLayer ignored = new ConfigListLayer(this)){
            maxDepth = addIntegerConfig("maxDepth", 128, 0, 65536);
            stretch = addDoubleConfig("stretch", 1);
        }
    }
    
    @Override public void onValueChanged() {
        super.onValueChanged();
        Registries.WORLD_RENDER_BEFORE_DEBUG_RENDER.register(this, getAsBoolean());
    }
    
    @Override public void beforeDebugRender(WorldRenderContext context) {
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().disableCullFace().enableDepthTest();
            double y = 1;
            double stretch = this.stretch.getAsDouble();
            double a = stretch * 2;
            float _stretch = (float)stretch;
            Vec3d camPos = context.camera().getPos();
            double d = Math.abs((camPos.y - y) * 128);
            double maxX = camPos.x + d;
            double minX = camPos.x - d;
            double maxZ = camPos.z + d;
            double minZ = camPos.z - d;
            if(maxX > a) maxX = a;
            if(minX < -a) minX = -a;
            if(maxZ > a) maxZ = a;
            if(minZ < -a) minZ = -a;
            buffer.clear();
            float _minX = (float)(minX - camPos.x);
            float _maxX = (float)(maxX - camPos.x);
            float _minZ = (float)(minZ - camPos.z);
            float _maxZ = (float)(maxZ - camPos.z);
            float _y = (float)(y - camPos.y);
            buffer.putFloats(_minX, _y, _minZ, (float)minX / _stretch, (float)minZ / _stretch);
            buffer.putFloats(_maxX, _y, _minZ, (float)maxX / _stretch, (float)minZ / _stretch);
            buffer.putFloats(_minX, _y, _maxZ, (float)minX / _stretch, (float)maxZ / _stretch);
            buffer.putFloats(_maxX, _y, _maxZ, (float)maxX / _stretch, (float)maxZ / _stretch);
            Matrix4f finalMatrix = new Matrix4f();
            context.matrixStack().peek().getPositionMatrix().mul(finalMatrix, finalMatrix);
            context.projectionMatrix().mul(finalMatrix, finalMatrix);
            buffer.setFinalMatrix(finalMatrix);
            buffer.setOutColor(new Vector4f(1, 1, 1, 1));
            buffer.setSetColor(new Vector4f(0, 0, 0, 1));
            buffer.setMaxDepth(maxDepth.getAsInt());
            buffer.render(Constants.DrawMode.TRIANGLE_STRIP);
        }
    }
    public static final VertexAttrib POSITION_COMPLEX = new VertexAttrib(VEC3F, VEC2F);
    public static final Shader mandelbrotSetVertexShader = newLPCVert("mandelbrot_set.glsl");
    public static final Shader mandelbrotSetFragmentShader = newLPCFrag("mandelbrot_set.glsl");
    public static final MandelbrotSetRenderProgram mandelbrotSetRenderProgram = new MandelbrotSetRenderProgram();
    private static final MandelbrotSetRenderBuffer buffer = new MandelbrotSetRenderBuffer(Constants.BufferMode.DYNAMIC_DRAW);
    
    public static class MandelbrotSetRenderProgram extends Program implements ShaderPrograms.WithFinalMatrix {
        public final Uniform.UniformMatrix4f matrixUniform = addUniform(new Uniform.UniformMatrix4f(this, "matrix"));
        public final Uniform.Uniform4f setColorUniform = addUniform(new Uniform.Uniform4f(this, "setColor"));
        public final Uniform.Uniform4f outColorUniform = addUniform(new Uniform.Uniform4f(this, "outColor"));
        public final Uniform.Uniform1i maxDepthUniform = addUniform(new Uniform.Uniform1i(this, "maxDepth"));
        public MandelbrotSetRenderProgram() {super(mandelbrotSetVertexShader, mandelbrotSetFragmentShader, POSITION_COMPLEX);}
        @Override public void setFinalMatrix(Matrix4f matrix) {matrixUniform.set(matrix);}
        public void setSetColor(Vector4f color){setColorUniform.set(color);}
        public void setOutColor(Vector4f color){outColorUniform.set(color);}
        public void setMaxDepth(int depth){maxDepthUniform.setValue(depth);}
    }
    public static class MandelbrotSetRenderBuffer extends ArrayListCachedRenderBuffer<MandelbrotSetRenderProgram> implements ShaderPrograms.WithFinalMatrix{
        public MandelbrotSetRenderBuffer(Constants.BufferMode bufferMode) {super(bufferMode, mandelbrotSetRenderProgram);}
        @Override public void setFinalMatrix(Matrix4f matrix) {program.setFinalMatrix(matrix);}
        public void setSetColor(Vector4f color){program.setSetColor(color);}
        public void setOutColor(Vector4f color){program.setOutColor(color);}
        public void setMaxDepth(int depth){program.setMaxDepth(depth);}
    }
}
